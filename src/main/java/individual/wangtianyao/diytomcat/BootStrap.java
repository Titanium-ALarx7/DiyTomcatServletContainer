package individual.wangtianyao.diytomcat;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.system.SystemUtil;
import individual.wangtianyao.diytomcat.catalina.*;
import individual.wangtianyao.diytomcat.http.Header;
import individual.wangtianyao.diytomcat.http.Request;
import individual.wangtianyao.diytomcat.http.Response;
import individual.wangtianyao.diytomcat.util.ServerXMLUtil;
import individual.wangtianyao.diytomcat.util.ThreadPoolUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BootStrap {

    public static void main(String[] args){
        Server server = new Server();
        server.start();
    }

}
