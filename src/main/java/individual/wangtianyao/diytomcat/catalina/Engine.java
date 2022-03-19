package individual.wangtianyao.diytomcat.catalina;

import individual.wangtianyao.diytomcat.util.ServerXMLUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Engine {
    private String defaultHost;
    private List<Host> hosts;
    private static Engine engine;

    public static Engine getEngine() {
        if(engine!=null) return engine;
        engine = new Engine();
        return engine;
    }

    private Engine(){
        this.defaultHost = ServerXMLUtil.getEngineDefaultHost();
        this.hosts = ServerXMLUtil.getHosts(this);
        checkDefault();
    }

    private void checkDefault(){
        if(getDefaultHost()==null) throw new RuntimeException("The default host "+defaultHost+" doesn't exist!");
    }

    public @Nullable Host getDefaultHost(){
        for(Host host:hosts){
            if(host.getName().equals(defaultHost)) return host;
        }
        return null;
    }

}
