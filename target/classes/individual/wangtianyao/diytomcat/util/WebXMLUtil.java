package individual.wangtianyao.diytomcat.util;

import cn.hutool.core.io.FileUtil;
import individual.wangtianyao.diytomcat.catalina.Context;
import individual.wangtianyao.diytomcat.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;


public class WebXMLUtil {
    /* 只要是一个Context，该方法即可指定一个welcome-file;
       虽然我们总是放在ROOT对应的uri:/之下
       因为Server会调用Context的docBase与fileName一起访问文件，所以没有文件会返回500状态码
     */
    public static String getWelcomeFileName(Context context){
        String xml = FileUtil.readUtf8String(Header.webXMLFile);
        Document d = Jsoup.parse(xml);
        Elements welcomeFiles = d.select("welcome-file");
        for(Element e: welcomeFiles){
            String fileName = e.text();
            File f = new File(context.getDocBase(), fileName);
            if(f.exists()) return f.getName();
        }
        // 不匹配即返回全局默认欢迎页
        return "index.html";
    }
}
