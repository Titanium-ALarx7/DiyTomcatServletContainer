package individual.wangtianyao.diytomcat.catalina;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private final Service service;
    public Server(){
        this.service = new Service(this);
    }

    public void start(){
        logJVM();
        init();
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

    private void init() {
        try {
            int port = 810;
            // NetUtil简单建立了一个localhost:port的Socket，检测连接是否成功。
            if (!NetUtil.isUsableLocalPort(port)) {
                System.out.println(port + "Port: " + port + " is unavailable. Please select another port.");
            }

            // 1. ServerSocket用于监听
            ServerSocket serverSocket = new ServerSocket(port);

            /*
             * 2.目前的TCP连接：自旋监听，accept()堵塞住的单线程
             * 每轮accept生成一个已完成连接的socket
             * 解析Request
             * 生成对应Response并回复
             */
            while (true) {
                // 1. serverSocket accept\阻塞，返回已完成连接socket
                Socket s = serverSocket.accept();

                // 匿名类的形式构建new Runnable
                Runnable task = () -> {
                    try {
                        Request reqs = new Request(s, service);
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
                            s.close();
                            return;
                        }

                        // 主页处理&静态资源处理
                        Response resp = new Response();

                        // 解析uri，生成对应状态码；
                        if (uri.equals("/")) handleWelcomePage(s, resp);
                        else {
                            // 在Diy Tomcat中，所有静态资源默认根目录为/webApps/ROOT;
                            // 即Header.rootFolder File类实例
                            String fileName = uri.substring(1, uri.length());
                            File file = FileUtil.file(context.getDocBase(), fileName);

                            if (file.exists()) {
                                // 响应timeConsuming任务
                                if (fileName.equals("wait1s.html")) {
                                    try {Thread.sleep(1000);}
                                    catch (Exception e) {e.printStackTrace();}
                                }

                                String fileContent = FileUtil.readUtf8String(file);
                                resp.getWriter().println(fileContent);
                                handleResponse200(s, resp);
                            }else handle404(s, uri);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally{
                        try{if(!s.isClosed()) s.close();}
                        catch(IOException e){e.printStackTrace();}
                    }
                };

                ThreadPoolUtil.run(task);
                // while循环结束
            }
            // serverSocket的try块结束
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void handleWelcomePage(Socket s, Response resp) throws IOException{
        String responseString = "Hello Diy Tomcat!";
        resp.getWriter().println(responseString);
        OutputStream os = s.getOutputStream();
        String respMessage = getResponseMessage200(resp);
        os.write(respMessage.getBytes(StandardCharsets.UTF_8));
        os.flush();
    }

    protected void handleResponse200(Socket s, Response resp) throws IOException{
        OutputStream os = s.getOutputStream();
        String respMessage = getResponseMessage200(resp);
        os.write(respMessage.getBytes(StandardCharsets.UTF_8));
        os.flush();
    }

    protected void handle404(Socket s, String uri) throws IOException{
        OutputStream os = s.getOutputStream();
        String responseBody = StrUtil.format(Header.page404, uri, uri);
        String responseMessage = Header.ResponseHeader404 + responseBody;
        byte[] bytes = responseMessage.getBytes(StandardCharsets.UTF_8);
        os.write(bytes);
        os.flush();
    }

    protected String getResponseMessage200(Response resp){
        return  Header.ResponseHeader200
                + Header.getHeaderEntryLine(Header.contentType, resp.getContentType())
                + "\r\n"
                + resp.getBodyString();
    }

}
