package individual.wangtianyao.diytomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import individual.wangtianyao.diytomcat.ContextFileChangeWatcher;
import individual.wangtianyao.diytomcat.classloader.WebAppClassLoader;
import individual.wangtianyao.diytomcat.exception.WebConfigDuplicatedException;
import individual.wangtianyao.diytomcat.http.ApplicationContext;
import individual.wangtianyao.diytomcat.http.StandardServletConfig;
import individual.wangtianyao.diytomcat.util.ContextXMLUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
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
    private Map<String, Map<String, String>> servlet_className_init_params;

    private Map<String, List<String>> url_filterClassName;
    private Map<String, List<String>> url_FilterNames;
    private Map<String, String> filterName_className;
    private Map<String, String> className_filterName;
    private Map<String, Map<String, String>> filter_className_init_params;

    private ServletContext servletContext;
    private Map<Class<?>, HttpServlet> servletPool;
    private Map<String, Filter> filterPool;
    private List<String> loadOnStartupServletClassNames;

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
        this.servletPool = new HashMap<>();
        this.filterPool = new HashMap<>();
        this.servlet_className_init_params=new HashMap<>();
        this.loadOnStartupServletClassNames = new ArrayList<>();

        this.url_filterClassName = new HashMap<>();
        this.url_FilterNames = new HashMap<>();
        this.filterName_className = new HashMap<>();
        this.className_filterName = new HashMap<>();
        this.filter_className_init_params = new HashMap<>();

        ClassLoader commonClassLoader = Thread.currentThread().getContextClassLoader();
        this.webAppClassLoader = new WebAppClassLoader(docBase, commonClassLoader);
        deploy();
    }

    public synchronized HttpServlet getServlet(Class<?> clazz)
            throws NoSuchMethodException, InstantiationException,
            InvocationTargetException, IllegalAccessException, ServletException {
        HttpServlet servlet = servletPool.get(clazz);
        if(servlet==null){
            servlet = (HttpServlet) clazz.getConstructor().newInstance();
            ServletContext servletContext = this.getServletContext();
            String className = clazz.getName();
            String servletName = classNameToServletName.get(className);
            Map<String, String> initParameters = servlet_className_init_params.get(className);
            ServletConfig servletConfig = new StandardServletConfig(servletContext, servletName, initParameters);
            servlet.init(servletConfig);
            servletPool.put(clazz, servlet);
        }
        return servlet;
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
        parseServletInitParams(d);
        parseLoadOnStartup(d);
        parseFilterMapping(d);
        parseFilterInitParams(d);
        handleLoadOnStartup();
        initFilters();
    }

    public void stop(){
        webAppClassLoader.stop();
        watcher.stop();
        destroyServlets();
    }

    private void destroyServlets(){
        Collection<HttpServlet> servlets = servletPool.values();
        for(HttpServlet servlet: servlets){
            servlet.destroy();
        }
    }

    private void initFilters(){
        Set<String>  classNames = className_filterName.keySet();
        for(String className:classNames){
            try{
                Class<?> clazz = this.getWebAppClassLoader().loadClass(className);
                Map<String, String> initParameters = filter_className_init_params.get(className);
                String filterName = className_filterName.get(className);
                FilterConfig filterConfig = new StandardFilterConfig(servletContext, filterName, initParameters);
                Filter filter = filterPool.get(clazz.toString());
                if(filter==null){
                    filter = (Filter) ReflectUtil.newInstance(clazz);
                    filter.init(filterConfig);
                    filterPool.put(className, filter);
                }
            }catch (Exception e){e.printStackTrace();}
        }
    }

    public void parseFilterMapping(Document d){
        Elements mappingurlElements = d.select("filter-mapping url-pattern");
        for (Element mappingurlElement : mappingurlElements) {
            String urlPattern = mappingurlElement.text();
            String filterName = mappingurlElement.parent().select("filter-name").first().text();

            List<String> filterNames= url_FilterNames.get(urlPattern);
            if(null==filterNames) {
                filterNames = new ArrayList<>();
                url_FilterNames.put(urlPattern, filterNames);
            }
            filterNames.add(filterName);
        }
        // class_name_filter_name
        Elements filterNameElements = d.select("filter filter-name");
        for (Element filterNameElement : filterNameElements) {
            String filterName = filterNameElement.text();
            String filterClass = filterNameElement.parent().select("filter-class").first().text();
            filterName_className.put(filterName, filterClass);
            className_filterName.put(filterClass, filterName);
        }
        // url_filterClassName

        Set<String> urls = url_FilterNames.keySet();
        for (String url : urls) {
            List<String> filterNames = url_FilterNames.get(url);
            if(null == filterNames) {
                filterNames = new ArrayList<>();
                url_FilterNames.put(url, filterNames);
            }
            for (String filterName : filterNames) {
                String filterClassName = filterName_className.get(filterName);
                List<String> filterClassNames = url_filterClassName.get(url);
                if(null==filterClassNames) {
                    filterClassNames = new ArrayList<>();
                    url_filterClassName.put(url, filterClassNames);
                }
                filterClassNames.add(filterClassName);
            }
        }
    }

    private void parseFilterInitParams(Document d) {
        Elements filterClassNameElements = d.select("filter-class");
        for (Element filterClassNameElement : filterClassNameElements) {
            String filterClassName = filterClassNameElement.text();

            Elements initElements = filterClassNameElement.parent().select("init-param");
            if (initElements.isEmpty())
                continue;

            Map<String, String> initParams = new HashMap<>();
            for (Element element : initElements) {
                String name = element.select("param-name").get(0).text();
                String value = element.select("param-value").get(0).text();
                initParams.put(name, value);
            }

            filter_className_init_params.put(filterClassName, initParams);
        }
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

    private void parseServletInitParams(Document d) {
        Elements es = d.select("servlet-class");
        for (Element e : es) {
            String servletClassName = e.text();
            Elements initEs = e.parent().select("init-param");
            if (initEs.isEmpty()) continue;
            Map<String, String> initParams = new HashMap<>();
            for (Element initE : initEs) {
                String name = initE.selectFirst("param-name").text();
                String value = initE.selectFirst("param-value").text();
                initParams.put(name, value);
            }
            this.servlet_className_init_params.putIfAbsent(servletClassName, initParams);
        }
        System.out.println("class_name_init_params:"+servlet_className_init_params);
    }

    public void parseLoadOnStartup(Document d){
        Elements es = d.select("load-on-startup");
        for(Element e: es){
            String loadName = e.parent().select("servlet-class").text();
            loadOnStartupServletClassNames.add(loadName);
        }
    }

    public void handleLoadOnStartup(){
        for(String loadName: loadOnStartupServletClassNames){
            try{
                Class<?> clazz = webAppClassLoader.loadClass(loadName);
                getServlet(clazz);
            }catch(Exception e){e.printStackTrace();}
        }
    }

    public List<Filter> getMatchedFilters(String uri){
        List<Filter> filters = new ArrayList<>();
        Set<String> patterns = url_filterClassName.keySet();
        Set<String> matchedPatterns = new HashSet<>();
        for(String pattern: patterns){
            if(match(pattern, uri)) matchedPatterns.add(pattern);
        }
        Set<String> matchedFilterClassNames = new HashSet<>();
        for(String pattern: matchedPatterns){
            List<String> filterClassName = url_filterClassName.get(pattern);
            matchedFilterClassNames.addAll(filterClassName);
        }
        for(String filterClassName: matchedFilterClassNames){
            Filter filter = filterPool.get(filterClassName);
            filters.add(filter);
        }
        return filters;
    }

    private boolean match(String pattern, String uri){
        if(StrUtil.equals(pattern, uri)) return true;
        if(StrUtil.equals(pattern, "/*")) return true;
        if(StrUtil.startWith(pattern, "/*.")){
            String patterExtName = StrUtil.subAfter(pattern, ".", false);
            String uriExtName = StrUtil.subAfter(uri, ".", false);
            if(StrUtil.equals(patterExtName, uriExtName)) return true;
        }
        return false;
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
