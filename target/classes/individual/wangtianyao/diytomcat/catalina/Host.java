package individual.wangtianyao.diytomcat.catalina;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import individual.wangtianyao.diytomcat.http.Header;
import individual.wangtianyao.diytomcat.util.ServerXMLUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


@SuppressWarnings("FieldMayBeFinal")
public class Host {
    private final String name;
    private Map<String, Context> contextMap;
    private Engine engine;

    public Host(String name, Engine engine){
        this.contextMap = new HashMap<>();
        this.name = name;
        this.engine = engine;

        scanContextsOnWebAppsFolder();
        scanContextsInServerXML();
        scanWarOnWebAppsFolder();
    }

    public void load(File folder){
        String path = folder.getName();
        if("ROOT".equals(path)) path="/";
        else path = "/"+path;
        String docBase = folder.getAbsolutePath();
        Context context = new Context(path, docBase, this, false);
        contextMap.put(context.getPath(), context);
    }

    public void loadWar(File warFile) {
        String fileName =warFile.getName();
        String folderName = StrUtil.subBefore(fileName,".",true);
        //看看是否已经有对应的 Context了
        Context context= getContext("/"+folderName);
        if(null!=context)
            return;
        //先看是否已经有对应的文件夹
        File folder = new File(Header.webappsFolder,folderName);
        if(folder.exists())
            return;
        //移动war文件，因为jar 命令只支持解压到当前目录下
        File tempWarFile = FileUtil.file(Header.webappsFolder, folderName, fileName);
        File contextFolder = tempWarFile.getParentFile();
        contextFolder.mkdir();
        FileUtil.copyFile(warFile, tempWarFile);
        //解压
        String command = "jar xvf " + fileName;
//		System.out.println(command);
        Process p = RuntimeUtil.exec(null, contextFolder, command);
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //解压之后删除临时war
        tempWarFile.delete();
        //然后创建新的 Context
        load(contextFolder);
    }

    private void scanWarOnWebAppsFolder() {
        File folder = FileUtil.file(Header.webappsFolder);
        File[] files = folder.listFiles();
        for (File file : files) {
            if(!file.getName().toLowerCase().endsWith(".war"))
                continue;
            loadWar(file);
        }
    }

    public String getName() {
        return name;
    }

    public Context getContext(String path) {
        return contextMap.get(path);
    }

    public Engine getEngine() {
        return engine;
    }

    private void addContextToContextMap(String key, Context context){
        this.contextMap.put(key, context);
    }

    public void reload(Context context){
        Logger log = Logger.getLogger("context-reload");
        log.info("Reloading context with name "+ context.getPath() +" has started.");
        String path = context.getPath();
        String docBase = context.getDocBase();
        boolean reloadable = context.isReloadable();
        context.stop();
        contextMap.remove(path);
        Context newContext = new Context(path, docBase, this, reloadable);
        contextMap.put(newContext.getPath(), newContext);
        log.info("Reloading context with name "+context.getPath() + "has completed.");

    }

    /*
       加载WebApps文件夹下的所有文件夹到Context数据结构,
       并将Context放到contextMap里存储。
       Context一定是一个文件目录
     */
    private void scanContextsOnWebAppsFolder(){
        File[] folders = Header.webappsFolder.listFiles();
        assert folders != null;
        for(File folder:folders){
            if(!folder.isDirectory()) continue;
            loadContextFromWebAppsFolder(folder);
        }
    }

    private void loadContextFromWebAppsFolder(File folder){
        // 将uri映射与context数据结构存储起来。
        String path=folder.getName();
        //webApps/ROOT用于存放主页
        if("ROOT".equals(path)) path="/";
        else path="/"+path;

        String docBase = folder.getAbsolutePath();
        Context context = new Context(path, docBase, this, true);

        this.contextMap.put(context.getPath(), context);
    }

    private void scanContextsInServerXML(){
        List<Context> contexts = ServerXMLUtil.getContexts(this);
        for(Context c: contexts) contextMap.put(c.getPath(), c);
    }

}
