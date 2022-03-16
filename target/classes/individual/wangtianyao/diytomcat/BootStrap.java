package individual.wangtianyao.diytomcat;

import cn.hutool.core.util.NetUtil;

import individual.wangtianyao.diytomcat.http.Request;
import individual.wangtianyao.diytomcat.http.Response;
import individual.wangtianyao.diytomcat.http.Header;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class BootStrap {

    public static void main(String[] args){
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

                String requestString = reqs.getRequestString();
                System.out.println("URI got from the Client Request: "+reqs.getUri()+"\r\n");
                System.out.println("Input Information from Explorer: \r\n" + requestString+"\r\n");

                Response resp = new Response();
                String response_head = "Content-Type:text/html\r\n\r\n";
                String responseString = "Hello Diy Tomcat!";
                resp.getWriter().println(responseString);

                // 依次在Socket的OS中写入200 Header，Optional Header，
                OutputStream os = s.getOutputStream();
                String rString = Header.ResponseHeader200
                        + Header.getHeaderEntryLine(Header.contentType, resp.getContentType())
                        + "\r\n"
                        + resp.getBodyString();
                os.write(rString.getBytes(StandardCharsets.UTF_8));

                os.flush();
                s.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
