package individual.wangtianyao.diytomcat;

import cn.hutool.core.util.NetUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

            ServerSocket serverSocket = new ServerSocket(port);

            while(true){
                Socket s = serverSocket.accept();
                InputStream is = s.getInputStream();
                int buffersize = 1024;
                byte[] buffer = new byte[buffersize];
                // read遇到EOF会返回-1
                is.read(buffer);
                String requestString = new String(buffer, StandardCharsets.UTF_8);
                System.out.println("Input Information from Explorer: \r\n" + requestString);

                OutputStream os = s.getOutputStream();
                String response_head = "HTTP/1.1 200 OK\r\n" +"Content-Type:text/html\r\n\r\n";
                String responseString = "Hello Diy Tomcat!";
                responseString = response_head+responseString;
                os.write(responseString.getBytes());

                os.flush();
                s.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
