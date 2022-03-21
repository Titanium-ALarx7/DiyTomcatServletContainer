package individual.wangtianyao.diytomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import individual.wangtianyao.diytomcat.util.ServerXMLUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Service {
    private final Engine engine;
    private final String name;
    private final Server server;
    private final List<Connector> connectors;

    public Service(Server server){
        this.server=server;
        this.name=ServerXMLUtil.getServiceName();
        this.engine=Engine.getEngine(this);
        this.connectors=ServerXMLUtil.getConnectors(this);
    }

    public Engine getEngine() {
        return engine;
    }

    public String getName() {
        return name;
    }

    public Server getServer() {
        return server;
    }

    public void start(){
        init();
    }

    private void init(){
        Logger log = Logger.getLogger("Service init");
        TimeInterval timeInterval = DateUtil.timer();
        for(Connector c:connectors) c.init();
        log.info("Initialization processed in "+timeInterval.intervalMs()+" ms");
        for(Connector c:connectors) c.start();
    }
}
