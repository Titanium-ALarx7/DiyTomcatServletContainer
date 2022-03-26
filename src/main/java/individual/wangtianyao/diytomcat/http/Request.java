package individual.wangtianyao.diytomcat.http;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import individual.wangtianyao.diytomcat.MiniBrowser;
import individual.wangtianyao.diytomcat.catalina.Context;
import individual.wangtianyao.diytomcat.catalina.Service;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

/*
 * 承担了解析/存储 HttpRequest的容器
 * 与ServerSocket调用accept()方法，得到的已完成连接socket绑定
 */
public class Request extends BaseRequest {
    private String requestString;
    private String uri;
    private Context context;
    private final Socket socket;
    private final Service service;
    private String method;
    private String queryString;
    private Cookie[] cookies;
    private HttpSession session;
    private final Map<String, String[]> parameterMap;
    private Map<String, String> headerMap;

    public Request(Socket socket, Service service) throws IOException{
        this.socket = socket;
        this.service=service;
        this.parameterMap = new HashMap<>();
        this.headerMap = new HashMap<>();
        parseHttpRequest();
        if(StrUtil.isEmpty(requestString)) return;
        parseUri();
        parseMethod();
        parseContext();
        parseParameters();
        parseHeaders();
        parseCookies();
        // 将uri对静态资源/abc/k.html拆分为 uri=“/k.html”; context.path="/abc";
        if(!"/".equals(context.getPath())){
            this.uri=StrUtil.removePrefix(uri, this.context.getPath());
            if(this.uri.equals("")) uri = "/";
        }
    }

    private void parseHttpRequest() throws IOException{
        InputStream is = this.socket.getInputStream();
        byte[] bytes = MiniBrowser.readBytes(is, false);
        this.requestString = new String(bytes, StandardCharsets.UTF_8);
    }

    private void parseUri(){
        String temp;

        temp = requestString.split("\r\n")[0].split(" ")[1];
        if(!StrUtil.contains(temp, '?')){
            this.uri=temp;
            return;
        }

        // remove queryString
        temp = StrUtil.subBefore(temp, '?', false);
        this.uri = temp;
    }

    private void parseContext(){
        // Context为一个@RequestMapping与dir的绝对路径的映射
        // 先查看该uri是否直接映射一个context(即uri代表了一个文件夹)，有则返回；此时uri将更新为”/“，之后会被welcomePage()处理。
        this.context = Objects.requireNonNull(service.getEngine().getDefaultHost()).getContext(this.uri);
        if(context!=null) return;

        // 如果没有现成context，就把uri按照dir + fileName静态资源/ Servlet url-mapping的形式剥离。
        String path = StrUtil.subBetween(uri, "/", "/");
        if(path==null) path="/";
        else path = "/"+path;
        this.context = Objects.requireNonNull(service.getEngine().getDefaultHost()).getContext(path);
        // 如果context解析不存在，默认context为ROOT文件夹下
        if(this.context==null) this.context = service.getEngine().getDefaultHost().getContext("/");
    }

    private void parseParameters(){
        if("GET".equals(this.getMethod())){
            String url = requestString.split(" ")[1];
            if(StrUtil.contains(url, '?')) queryString = url.split("\\?")[1];
        }

        if("POST".equals(this.getMethod())) queryString= requestString.split("\r\n\r\n")[1];

        if(queryString==null) return;
        queryString = URLUtil.decode(queryString);
        String[] parameterValues = queryString.split("&");
        if(parameterValues!=null){
            for(String parameterValue: parameterValues){
                String[] nameValues = parameterValue.split("=");
                String name = nameValues[0];
                String value = nameValues[1];
                String[] values = parameterMap.get(name);
                if(values==null){
                    values = new String[]{value};
                    parameterMap.put(name, values);
                }else{
                    values = ArrayUtil.append(values, value);
                    parameterMap.put(name, values);
                }
            }
        }
    }

    private void parseMethod(){
        this.method = this.requestString.split("\r\n")[0].split(" ")[0];
    }

    private void parseHeaders(){
        StringReader stringReader = new StringReader(requestString);
        List<String> lines = new ArrayList<>();
        IoUtil.readLines(stringReader, lines);
        for(int i=1;i< lines.size();i++){
            String line = lines.get(i);
            if(line.length()==0) break;
            String[] segs = line.split(":");
            headerMap.put(segs[0].toLowerCase(), segs[1]);
        }
    }

    private void parseCookies(){
        List<Cookie> cookieList = new ArrayList<>();
        String cookies = headerMap.get("cookie");
        if(cookies!=null){
            String[] pairs = StrUtil.split(cookies,";");
            for(String pair: pairs){
                if(StrUtil.isBlank(pair)) continue;
                String[] segs = StrUtil.split(pair, "=");
                String name = segs[0].trim();
                String value = segs[1].trim();
                Cookie cookie = new Cookie(name, value);
                cookieList.add(cookie);
            }
        }
        this.cookies = ArrayUtil.toArray(cookieList, Cookie.class);
    }

    public Cookie[] getCookies(){
        return cookies;
    }

    public HttpSession getSession(){
        return session;
    }

    public void setSession(HttpSession session){
        this.session=session;
    }

    public String getJSessionIdFromCookie(){
        if(cookies==null) return null;
        for(Cookie cookie: cookies){
            if(cookie.getName().equals("JSESSIONID")) return cookie.getValue();
        }
        return null;
    }

    public String getMethod() {
        return method;
    }

    public String getRequestString() {
        return requestString;
    }

    public String getUri() {
        return uri;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public ServletContext getServletContext() {
        return context.getServletContext();
    }

    public String getRealPath(String path){
        return getServletContext().getRealPath(path);
    }

    public String getParameter(String name){
        String[] values = parameterMap.get(name);
        if(values!=null && values.length!=0) return values[0];
        return null;
    }

    public Map<String, String[]> getParameterMap() {
        return parameterMap;
    }

    public Enumeration<String> getParameterNames(){
        return Collections.enumeration(parameterMap.keySet());
    }

    public String[] getParameterValues(String name){
        return parameterMap.get(name);
    }

    public String getHeader(String name){
        if(name==null) return null;
        name = name.toLowerCase();
        return headerMap.get(name);
    }

    public Enumeration getHeaderNames(){
        Set<?> keys = headerMap.keySet();
        return Collections.enumeration(keys);
    }

    public int getIntHeaders(String name){
        String val = headerMap.get(name);
        return Integer.parseInt(val);
    }

    public String getLocalAddr(){
        return socket.getLocalAddress().getHostAddress();
    }

    public String getLocalName(){
        return socket.getLocalAddress().getHostName();
    }

    public int getLocalPort(){
        return socket.getLocalPort();
    }

    public String getProtocol(){
        return requestString.split(" ")[2];
    }

    public String getRemoteAddr(){
        InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
        String temp = isa.getAddress().toString();
        return temp.split("/")[1];
    }

    public String getRemoteHost(){
        InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
        return isa.getHostName();
    }

    public int getRemotePort(){
        return socket.getPort();
    }

    public String getScheme(){
        return "http";
    }

    public String getServerName(){
        return getHeader("host").trim();
    }

    public int getServerPort(){
        return getLocalPort();
    }

    public String getContextPath(){
        String result = this.context.getPath();
        if(result.equals("/")) return "";
        return result;
    }

    public String getRequestURI(){
        return uri;
    }
    public StringBuffer getRequestURL(){
        StringBuffer url = new StringBuffer();
        String scheme = getScheme();
        int port = getServerPort();
        if(port<0) port=80;
        url.append(scheme);
        url.append("://");
        url.append(getServerName());
        if((scheme.equals("http")&&(port!=80))||(scheme.equals("https")&&(port!=443))){
            url.append(":");
            url.append(port);
        }
        url.append(getRequestURI());
        return url;
    }

    public String getServletPath(){
        return uri;
    }
}
