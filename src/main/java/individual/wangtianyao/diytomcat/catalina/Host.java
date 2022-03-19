package individual.wangtianyao.diytomcat.catalina;

import individual.wangtianyao.diytomcat.http.Header;
import individual.wangtianyao.diytomcat.util.ServerXMLUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
        Context context = new Context(path, docBase);

        this.contextMap.put(context.getPath(), context);
    }

    private void scanContextsInServerXML(){
        List<Context> contexts = ServerXMLUtil.getContexts();
        for(Context c: contexts) contextMap.put(c.getPath(), c);
    }

}
