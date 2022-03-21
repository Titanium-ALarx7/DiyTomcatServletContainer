package individual.wangtianyao.diytomcat.catalina;

import cn.hutool.core.util.NetUtil;
import individual.wangtianyao.diytomcat.http.Request;
import individual.wangtianyao.diytomcat.http.Response;
import individual.wangtianyao.diytomcat.util.ThreadPoolUtil;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class Connector implements Runnable{

    int port;
    private final Service service;

    public void setPort(int port) {
        this.port = port;
    }

    public Service getService() {
        return service;
    }

    public Connector(Service service){
        this.service=service;
    }

    public void init(){
        Logger log = Logger.getLogger("Connector init");
        log.info("Starting Protocal Handler ==> http-bio-"+port);
    }

    public void start(){
        Logger log = Logger.getLogger("Connector init");
        log.info("Starting Protocal Handler ==> http-bio-"+port);
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            int port = this.port;
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
                    Request reqs=null;
                    try{reqs = new Request(s, service);}
                    catch(IOException e){e.printStackTrace();};
                    assert reqs != null;
                    Context context = reqs.getContext();
                    Response resp = new Response();

                    HttpProcessor pro = new HttpProcessor();
                    pro.execute(s, reqs, resp);
                };

                ThreadPoolUtil.run(task);
                // while循环结束
            }
            // serverSocket的try块结束
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
