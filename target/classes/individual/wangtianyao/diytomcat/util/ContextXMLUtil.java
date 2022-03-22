package individual.wangtianyao.diytomcat.util;

import cn.hutool.core.io.FileUtil;
import individual.wangtianyao.diytomcat.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ContextXMLUtil {
    public static String getWatchedResource(){

        String xml = FileUtil.readUtf8String(Header.contextXMLFile);
        Document d = Jsoup.parse(xml);
        Element watchedResource = d.selectFirst("WatchedResource");
        return watchedResource.text();
    }
}
