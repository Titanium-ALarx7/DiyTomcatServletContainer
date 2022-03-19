package individual.wangtianyao.diytomcat.util;

import cn.hutool.core.io.FileUtil;
import individual.wangtianyao.diytomcat.catalina.Context;
import individual.wangtianyao.diytomcat.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class ServerXMLUtil {

    public static List<Context> getContexts(){
        String xml = FileUtil.readUtf8String(Header.serverXMLFile);

        Document d = Jsoup.parse(xml);
        Elements es = d.select("Context");
        List<Context> result = new ArrayList<>();

        for(Element e:es){
            String path = e.attr("path");
            String docBase = e.attr("docBase");
            Context context = new Context(path, docBase);
            result.add(context);
        }
        return result;
    }

    public static String getHostName(){
        String xml = FileUtil.readUtf8String(Header.serverXMLFile);
        Document d = Jsoup.parse(xml);
        Element host = d.select("host").first();
        return host.attr("name");
    }
}
