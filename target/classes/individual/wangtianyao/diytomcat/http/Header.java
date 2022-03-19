package individual.wangtianyao.diytomcat.http;

import cn.hutool.system.SystemUtil;

import java.io.File;

public class Header {
    public static final String ResponseHeader200="HTTP/1.1 200 OK\r\n";
    public static final String contentType="Content-Type";

    // user.dir是项目文件夹的绝对路径，file第一个形参为parent
    public final static File webappsFolder = new File(SystemUtil.get("user.dir"),"webapps");
    public final static File rootFolder = new File(webappsFolder,"ROOT");

    public static String getHeaderEntryLine(String headerKey, String headerValue){
        return headerKey+":"+headerValue+"\r\n";
    }
}
