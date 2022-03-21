package individual.wangtianyao.diytomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.system.SystemUtil;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private final Service service;
    public Server(){
        this.service = new Service(this);
    }

    public void start(){
        TimeInterval t = DateUtil.timer();
        Logger log = Logger.getLogger("Server init");
        logJVM();
        init();
        log.info("Server startup in "+ t.intervalMs() +" ms.");
    }

    private static void logJVM(){
        Logger log = Logger.getAnonymousLogger();
        log.log(Level.INFO, "Server Version: Diy Tomcat/1.01");
        log.log(Level.INFO, "Server Built: 2022/3/12");
        log.log(Level.INFO, "OS Name\t"+ SystemUtil.get("os.name"));
        log.log(Level.INFO, "OS Version\t"+ SystemUtil.get("os.version"));
        log.log(Level.INFO, "Architecture\t"+ SystemUtil.get("os.arch"));
        log.log(Level.INFO, "JVM Version\t"+ SystemUtil.get("java.runtime.version"));
        log.log(Level.INFO, "Java Home\t"+ SystemUtil.get("java.home"));
        log.log(Level.INFO, "JVM Vendor\t"+ SystemUtil.get("java.vm.specification.vendor"));
    }

    private void init() {
        service.start();
    }

}
