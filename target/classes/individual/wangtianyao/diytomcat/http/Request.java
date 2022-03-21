package individual.wangtianyao.diytomcat.http;

import cn.hutool.core.util.StrUtil;
import individual.wangtianyao.diytomcat.MiniBrowser;
import individual.wangtianyao.diytomcat.catalina.Context;
import individual.wangtianyao.diytomcat.catalina.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/*
 * 承担了解析/存储 HttpRequest的容器
 * 与ServerSocket调用accept()方法，得到的已完成连接socket绑定
 */
public class Request {
    private String requestString;
    private String uri;
    private final Socket socket;
    private Context context;
    private final Service service;

    public Request(Socket socket, Service service) throws IOException{
        this.socket = socket;
        this.service=service;
        parseHttpRequest();
        if(StrUtil.isEmpty(requestString)) return;
        parseUri();
        parseContext();
        // 将uri对静态资源/abc/k.html拆分为 uri=“/k.html”; context.path="/abc";
        if(!"/".equals(context.getPath())){
            this.uri=StrUtil.removePrefix(uri, this.context.getPath());
            if(this.uri.equals("")) uri = "/";
        }
    }

    private void parseHttpRequest() throws IOException{
        InputStream is = this.socket.getInputStream();
        byte[] bytes = MiniBrowser.readBytes(is);
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
        // 先查看该uri是否直接映射一个context，有则返回
        this.context = Objects.requireNonNull(service.getEngine().getDefaultHost()).getContext(this.uri);
        if(context!=null) return;

        // 如果没有现成context，就把uri按照dir + fileName静态资源的形式剥离。
        String path = StrUtil.subBetween(uri, "/", "/");
        if(path==null) path="/";
        else path = "/"+path;
        this.context = Objects.requireNonNull(service.getEngine().getDefaultHost()).getContext(path);
        if(this.context==null) this.context = service.getEngine().getDefaultHost().getContext("/");
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
}
