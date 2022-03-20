package individual.wangtianyao.diytomcat.catalina;

import individual.wangtianyao.diytomcat.util.ServerXMLUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Engine {
    private final String defaultHost;
    private final List<Host> hosts;
    private static Engine engine;
    private final Service service;

    public static Engine getEngine(Service s) {
        if(engine!=null) return engine;
        engine = new Engine(s);
        return engine;
    }

    private Engine(Service s){
        this.service= s;
        this.defaultHost = ServerXMLUtil.getEngineDefaultHost();
        this.hosts = ServerXMLUtil.getHosts(this);
        checkDefault();
    }

    public Service getService() {
        return service;
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
