package individual.wangtianyao.diytomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Context {
    private String path;
    private String docBase;

    public Context(String path, String docBase) {
        TimeInterval timer = DateUtil.timer();
        this.path = path;
        this.docBase = docBase;
        Logger log = Logger.getLogger("context-info");
        log.log(Level.INFO, "Deploying web application directory "+this.docBase);
        log.log(Level.INFO, "Deployment of web application directory "+this.docBase+ " has finished in"+timer.intervalMs());
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDocBase() {
        return docBase;
    }

    public void setDocBase(String docBase) {
        this.docBase = docBase;
    }
}
