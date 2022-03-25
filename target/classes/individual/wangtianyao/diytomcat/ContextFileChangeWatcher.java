package individual.wangtianyao.diytomcat;

import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.WatchUtil;
import cn.hutool.core.io.watch.Watcher;
import individual.wangtianyao.diytomcat.catalina.Context;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.logging.Logger;

public class ContextFileChangeWatcher {
    private WatchMonitor monitor;
    private boolean stop =false;

    public ContextFileChangeWatcher(Context context){
        this.monitor = WatchUtil.createAll(context.getDocBase(), Integer.MAX_VALUE, new Watcher() {
            @Override
            public void onCreate(WatchEvent<?> watchEvent, Path path) {
                dealWith(watchEvent);
            }

            @Override
            public void onModify(WatchEvent<?> watchEvent, Path path) {
                dealWith(watchEvent);
            }

            @Override
            public void onDelete(WatchEvent<?> watchEvent, Path path) {
                dealWith(watchEvent);
            }

            @Override
            public void onOverflow(WatchEvent<?> watchEvent, Path path) {
                dealWith(watchEvent);
            }

            private void dealWith(WatchEvent<?> event){
                synchronized(ContextFileChangeWatcher.class){
                    String fileName = event.context().toString();
                    if(stop) return;
                    if(fileName.endsWith(".jar") || fileName.endsWith(".class") || fileName.endsWith(".xml")){
                        stop=true;
                        Logger log = Logger.getLogger("context file change watcher");
                        log.info(ContextFileChangeWatcher.this + "has monitored important file changes: "+fileName);
                        context.reload();
                    }
                }
            }
        });

        this.monitor.setDaemon(true);
    }

    public void start(){
        monitor.start();
    }

    public void stop(){
        monitor.close();
    }
}
