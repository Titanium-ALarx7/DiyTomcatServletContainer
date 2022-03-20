package individual.wangtianyao.diytomcat;

import individual.wangtianyao.diytomcat.catalina.Server;


public class BootStrap {
    public static void main(String[] args){
        Server server = new Server();
        server.start();
    }

}
