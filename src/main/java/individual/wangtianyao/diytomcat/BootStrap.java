package individual.wangtianyao.diytomcat;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.system.SystemUtil;
import individual.wangtianyao.diytomcat.catalina.Context;
import individual.wangtianyao.diytomcat.http.Header;
import individual.wangtianyao.diytomcat.http.Request;
import individual.wangtianyao.diytomcat.http.Response;
import individual.wangtianyao.diytomcat.util.ThreadPoolUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BootStrap {
    // webApps文件夹的扫描容器
    public static Map<String, Context> contextMap = new HashMap<>();

    public static void main(String[] args){
        logJVM();

        scanContextsOnWebAppsFolder();

        try {
            int port = 810;
            // NetUtil简单建立了一个localhost:port的Socket，检测连接是否成功。
            if(!NetUtil.isUsableLocalPort(port)){
                System.out.println(port + "Port: "+port+ " is unavailable. Please select another port.");
            }

            // 1. ServerSocket用于监听
            ServerSocket serverSocket = new ServerSocket(port);

            /*
             * 2.目前的TCP连接：自旋监听，accept()堵塞住的单线程
             * 每轮accept生成一个已完成连接的socket
             * 解析Request
             * 生成对应Response并回复
             */
            while(true){
                // 1. serverSocket accept\阻塞，返回已完成连接socket
                Socket s = serverSocket.accept();

                // 匿名类的形式构建new Runnable
                Runnable task = new Runnable() {
                    @Override
                    public void run(){
                        try {
                            Request reqs = new Request(s);
                            Context context = reqs.getContext();

                            // firefox的请求，会自动给null的uri加上 ‘/’
                            String requestString = reqs.getRequestString();
                            String uri = reqs.getUri();
                            System.out.println("URI got from the Client Request: " + uri + "\r\n");
                            System.out.println("Input Information from Explorer: \r\n" + requestString + "\r\n");

                            // 以下进入基于uri解析的Response处理流程
                            // MiniBrowser会自动加上'/'
                            // uri理论上起码是‘/’，所以null类似于异常处理
                            if (uri == null) {
                                // 之后可以添加欢迎页处理
                                s.close();
                                return;
                            }

                            // 主页处理&静态资源处理
                            Response resp = new Response();
                            if (uri.equals("/")) {
                                String responseString = "Hello Diy Tomcat!";
                                resp.getWriter().println(responseString);
                            } else {
                                String fileName = uri.substring(1, uri.length());
                                // 在Diy Tomcat中，所有静态资源默认根目录为/webApps/ROOT;
                                // 即Header.rootFolder File类实例
                                File file = FileUtil.file(context.getDocBase(), fileName);
                                if (file.exists()) {
                                    String fileContent = FileUtil.readUtf8String(file);
                                    resp.getWriter().println(fileContent);

                                    // 响应timeConsuming任务
                                    if (fileName.equals("wait1s.html")) {
                                        try {
                                            Thread.sleep(1000);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }else resp.getWriter().println("404 File Not Found");
                            }

                            // 依次在Socket的OS中写入200 Header，Optional Header，
                            OutputStream os = s.getOutputStream();
                            String rString = Header.ResponseHeader200
                                    + Header.getHeaderEntryLine(Header.contentType, resp.getContentType())
                                    + "\r\n"
                                    + resp.getBodyString();
                            /*
                             * Socket的OutPutStream如果多次写，即使不flush，写满了也有可能自动发出。
                             * 因而MiniBrowser只能一次性读取所有字节。
                             */
                            os.write(rString.getBytes(StandardCharsets.UTF_8));
                            os.flush();
                            s.close();
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                };

                ThreadPoolUtil.run(task);
                // while循环结束
            }
        // serverSocket的try块结束
        }catch(IOException e){
            e.printStackTrace();
        }
    }


    // 加载WebApps文件夹下的所有文件夹到Context数据结构,并将Context放到contextMap里存储。
    // Context一定是一个文件目录
    private static void scanContextsOnWebAppsFolder(){
        File[] folders = Header.webappsFolder.listFiles();
        assert folders != null;
        for(File folder:folders){
            if(!folder.isDirectory()) continue;
            loadContextFromWebAppsFolder(folder);
        }
    }

    private static void loadContextFromWebAppsFolder(File folder){
        // 将uri映射与context数据结构存储起来。
        String path=folder.getName();
        //webApps/ROOT用于存放主页
        if("ROOT".equals(path)) path="/";
        else path="/"+path;

        String docBase = folder.getAbsolutePath();
        Context context = new Context(path, docBase);

        contextMap.put(context.getPath(), context);
    }

    private static void logJVM(){
        Logger log = Logger.getAnonymousLogger();
        log.log(Level.INFO, "Server Version: Diy Tomcat/1.01");
        log.log(Level.INFO, "Server Built: 2022/3/12");
        log.log(Level.INFO, "OS Name\t"+ SystemUtil.get("os.name"));
        log.log(Level.INFO, "OS Version\t"+ SystemUtil.get("os.version"));
        log.log(Level.INFO, "Architecture\t"+ SystemUtil.get("os.arch"));
        log.log(Level.INFO, "JVM Version\t"+ SystemUtil.get("java.runtime.version"));
        log.log(Level.INFO, "Java Home\t"+ SystemUtil.get("java.home"));
        log.log(Level.INFO, "JVM Vendor\t"+ SystemUtil.get("java.vm.specification.vendor"));
    }

}
