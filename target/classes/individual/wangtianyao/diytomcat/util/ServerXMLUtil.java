package individual.wangtianyao.diytomcat.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import individual.wangtianyao.diytomcat.catalina.*;
import individual.wangtianyao.diytomcat.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class ServerXMLUtil {

    public static List<Context> getContexts(Host host){
        String xml = FileUtil.readUtf8String(Header.serverXMLFile);
        Document d = Jsoup.parse(xml);
        Elements es = d.select("Context");
        List<Context> result = new ArrayList<>();

        for(Element e:es){
            String path = e.attr("path");
            String docBase = e.attr("docBase");
            boolean reloadable = Convert.toBool(e.attr("reloadable"), true);
            Context context = new Context(path, docBase, host, reloadable);
            result.add(context);
        }
        return result;
    }

    public static String getServiceName(){
        String xml = FileUtil.readUtf8String(Header.serverXMLFile);
        Document d = Jsoup.parse(xml);
        Element service = d.select("Service").first();
        return service.attr("name");
    }

    public static String getEngineDefaultHost() {
        String xml = FileUtil.readUtf8String(Header.serverXMLFile);
        Document d =Jsoup.parse(xml);
        Element engine = d.selectFirst("engine");
        return engine.attr("defaultHost");
    }

    public static List<Host> getHosts(Engine engine){
        List<Host> result = new ArrayList<>();
        String xml = FileUtil.readUtf8String(Header.serverXMLFile);
        Document d = Jsoup.parse(xml);
        Elements es = d.select("Host");
        for(Element e: es){
            String name = e.attr("name");
            Host host = new Host(name, engine);
            result.add(host);
        }
        return result;
    }

    public static List<Connector> getConnectors(Service service){
        List<Connector> result = new ArrayList<>();
        String xml = FileUtil.readUtf8String(Header.serverXMLFile);
        Document d = Jsoup.parse(xml);
        Elements es = d.select("Connector");
        for(Element e:es){
            int port = Integer.parseInt(e.attr("port"));
            String compression = e.attr("compression");
            int compressionMinSize = Convert.toInt(e.attr("compressionMinSize"), 0);
            String noCompressionUserAgents = e.attr("noCompressionUserAgents");
            String compressableMimeType = e.attr("compressableMimeType");
            Connector c = new Connector(service);
            c.setPort(port);
            c.setCompression(compression);
            c.setCompressableMimeType(compressableMimeType);
            c.setNoCompressionUserAgents(noCompressionUserAgents);
            c.setCompressableMimeType(compressableMimeType);
            c.setCompressionMinSize(compressionMinSize);
            result.add(c);
        }
        return result;
    }


}
