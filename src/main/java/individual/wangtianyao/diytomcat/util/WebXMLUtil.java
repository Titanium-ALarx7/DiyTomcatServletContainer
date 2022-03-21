package individual.wangtianyao.diytomcat.util;

import cn.hutool.core.io.FileUtil;
import individual.wangtianyao.diytomcat.catalina.Context;
import individual.wangtianyao.diytomcat.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class WebXMLUtil {
    private static Map<String, String> mimeTypeMapping =  new HashMap<>();

    /*
       只要是一个Context，该方法即可指定一个welcome-file;
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

    public static synchronized String getMimeType(String suffix){
        if(mimeTypeMapping.isEmpty()) initMimeType();
        String mimeType = mimeTypeMapping.get(suffix);
        if(mimeType==null) return "text/html";
        return mimeType;
    }

    private static void initMimeType(){
        String xml = FileUtil.readUtf8String(Header.webXMLFile);
        Document d = Jsoup.parse(xml);
        Elements mimeTypes = d.select("mime-mapping");
        for(Element e: mimeTypes){
            String suffix = e.select("suffix").first().text();
            String mimeType = e.select("mime-type").first().text();
            mimeTypeMapping.put(suffix, mimeType);
        }
    }


}
