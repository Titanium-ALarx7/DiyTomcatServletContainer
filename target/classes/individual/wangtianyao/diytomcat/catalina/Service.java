package individual.wangtianyao.diytomcat.catalina;

import individual.wangtianyao.diytomcat.util.ServerXMLUtil;

public class Service {
    private final Engine engine;
    private final String name;
    private final Server server;

    public Service(Server server){
        this.server=server;
        this.name=ServerXMLUtil.getServiceName();
        this.engine=Engine.getEngine(this);
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
}
