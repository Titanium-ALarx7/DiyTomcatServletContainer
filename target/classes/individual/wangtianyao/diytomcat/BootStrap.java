package individual.wangtianyao.diytomcat;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NetUtil;

import cn.hutool.core.util.StrUtil;
import individual.wangtianyao.diytomcat.http.Request;
import individual.wangtianyao.diytomcat.http.Response;
import individual.wangtianyao.diytomcat.http.Header;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BootStrap {

    public static void main(String[] args){
        Properties ps = System.getProperties();
        Logger logInfo = Logger.getLogger("INFO");

        /*
        for(Object key: ps.keySet()){
            logInfo.log(Level.INFO, key.toString()+" ======> "+ ps.getProperty((String) key));
        }
        */

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
                Socket s = serverSocket.accept();
                Request reqs = new Request(s);

                // firefox的请求，会自动给null的uri加上 ‘/’
                String requestString = reqs.getRequestString();
                String uri = reqs.getUri();
                System.out.println("URI got from the Client Request: "+ uri +"\r\n");
                System.out.println("Input Information from Explorer: \r\n" + requestString+"\r\n");

                // 以下进入基于uri解析的Response处理流程
                // MiniBrowser会自动加上'/'
                // uri理论上起码是‘/’，所以null类似于异常处理
                if(uri==null) {
                    // 之后可以添加欢迎页处理
                    s.close();
                    continue;
                }

                // 主页处理&静态资源处理
                Response resp = new Response();
                if(uri.equals("/")){
                    String responseString = "Hello Diy Tomcat!";
                    resp.getWriter().println(responseString);
                }else{
                    String fileName = uri.substring(1, uri.length());
                    File file = FileUtil.file(Header.rootFolder, fileName);
                    if(file.exists()){
                        String fileContent = FileUtil.readUtf8String(file);
                        resp.getWriter().println(fileContent);

                        // 响应timeConsuming任务
                        if(fileName.equals("wait1s.html")){
                            try{Thread.sleep(1000);}
                            catch(Exception e){e.printStackTrace();}
                        }

                    }else{
                        resp.getWriter().println("404 File Not Found");
                    }
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
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
