package individual.wangtianyao.diytomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import individual.wangtianyao.diytomcat.ContextFileChangeWatcher;
import individual.wangtianyao.diytomcat.classloader.WebAppClassLoader;
import individual.wangtianyao.diytomcat.exception.WebConfigDuplicatedException;
import individual.wangtianyao.diytomcat.http.ApplicationContext;
import individual.wangtianyao.diytomcat.util.ContextXMLUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public class Context {
    private String path;
    private String docBase;
    private Host host;
    private boolean reloadable;
    // contextWebXMLFile表示docBase/WEB-INF/web.xml
    private final File contextWebXMLFile;
    private WebAppClassLoader webAppClassLoader;
    private ContextFileChangeWatcher watcher;

    private final Map<String, String> urlToServletClassName;
    private final Map<String, String> urlToServletName;
    private final Map<String, String> servletNameToClassName;
    private final Map<String, String> classNameToServletName;

    private ServletContext servletContext;

    public Context(String path, String docBase, Host host, boolean reloadable) {
        this.path = path;
        this.docBase = docBase;
        this.host = host;
        this.reloadable = reloadable;
        // 解析context目录下的WEB-INF文件夹，没有就跳过。
        this.contextWebXMLFile = new File(docBase, ContextXMLUtil.getWatchedResource());
        this.urlToServletName = new HashMap<>();
        this.urlToServletClassName = new HashMap<>();
        this.servletNameToClassName = new HashMap<>();
        this.classNameToServletName = new HashMap<>();

        this.servletContext = new ApplicationContext(this);

        ClassLoader commonClassLoader = Thread.currentThread().getContextClassLoader();
        this.webAppClassLoader = new WebAppClassLoader(docBase, commonClassLoader);
        deploy();
    }

    public void stop(){
        webAppClassLoader.stop();
        watcher.stop();
    }

    public void reload(){
        host.reload(this);
    }

    private void deploy(){
        TimeInterval timer = DateUtil.timer();
        Logger log = Logger.getLogger("context-info");
        log.info("Deploying web application directory "+ this.docBase);
        init();
        if(reloadable){
            watcher=new ContextFileChangeWatcher(this);
            watcher.start();
        }
        log.info("Deployment of web application directory "+ this.docBase
                + " has finished in "+ timer.intervalMs() + " ms.");
    }

    private void init(){
        if(!contextWebXMLFile.exists()) return;

        try{ checkDuplicated();
        } catch(WebConfigDuplicatedException e){e.printStackTrace(); return;}

        String xml = FileUtil.readUtf8String(contextWebXMLFile);
        Document d = Jsoup.parse(xml);
        parseServletMapping(d);
    }

    private void parseServletMapping(Document d){
        //urlToServletName
        Elements mappingUrlElements = d.select("servlet-mapping url-pattern");
        for(Element e: mappingUrlElements){
            String urlPattern = e.text();
            String servletName = e.parent().selectFirst("servlet-name").text();
            this.urlToServletName.put(urlPattern, servletName);
        }

        Elements servletNameElements = d.select("servlet servlet-name");
        for(Element e: servletNameElements){
            String servletName = e.text();
            String servletClass = e.parent().selectFirst("servlet-class").text();
            this.servletNameToClassName.put(servletName, servletClass);
            this.classNameToServletName.put(servletClass, servletName);
        }

        Set<String> urls = this.urlToServletName.keySet();
        for(String url: urls){
            String servletName = this.urlToServletName.get(url);
            String servletClassName = this.servletNameToClassName.get(servletName);
            this.urlToServletClassName.put(url, servletClassName);
        }
    }

    private void checkDuplicated() throws WebConfigDuplicatedException{
        String xml = FileUtil.readUtf8String(this.contextWebXMLFile);
        Document d = Jsoup.parse(xml);
        checkServletMappingDuplicated(d, "servlet-mapping url-pattern", "URL is duplicated. Please check and fix.");
        checkServletMappingDuplicated(d, "servlet servlet-name", "Servlet-name is duplicated. Please check and fix.");
        checkServletMappingDuplicated(d, "servlet servlet-class", "Servlet-class is duplicated. Please check and fix.");
    }

    private void checkServletMappingDuplicated(Document d, String mapping, String desc)
            throws WebConfigDuplicatedException{
        List<String> contents = new ArrayList<>();
        Elements es = d.select(mapping);
        for(Element e: es){
            contents.add(e.text());
        }
        Collections.sort(contents);
        for(int i=0;i<contents.size()-1;i++)
            if(contents.get(i).equals(contents.get(i+1)))
                throw new WebConfigDuplicatedException(StrUtil.format(desc, contents.get(i)));
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDocBase() {
        return docBase;
    }

    public void setDocBase(String docBase) {
        this.docBase = docBase;
    }

    public String getServletClassName(String uri){
        return this.urlToServletClassName.get(uri);
    }

    public WebAppClassLoader getWebAppClassLoader() {
        return webAppClassLoader;
    }

    public boolean isReloadable() {
        return reloadable;
    }

    public void setReloadable(boolean reloadable) {
        this.reloadable = reloadable;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }
}
