package individual.wangtianyao.diytomcat.watcher;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.WatchUtil;
import cn.hutool.core.io.watch.Watcher;
import individual.wangtianyao.diytomcat.catalina.Host;
import individual.wangtianyao.diytomcat.http.Header;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.StandardWatchEventKinds;
import java.util.Locale;

public class WarFileWatcher {
    private WatchMonitor monitor;
    public WarFileWatcher(Host host) {
        this.monitor = WatchUtil.createAll(Header.webappsFolder, 1, new Watcher() {
            private void dealwith(WatchEvent<?> event, Path currentPath){
                synchronized (WarFileWatcher.class){
                    String fileName = event.context().toString();
                    if(fileName.toLowerCase(Locale.ROOT).endsWith(".war")
                            && StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())){
                        File warFile = FileUtil.file(Header.webappsFolder, fileName);
                        host.loadWar(warFile);
                    }
                }
            }
            @Override
            public void onCreate(WatchEvent<?> watchEvent, Path path) {
                dealwith(watchEvent, path);
            }

            @Override
            public void onModify(WatchEvent<?> watchEvent, Path path) {
                dealwith(watchEvent, path);
            }

            @Override
            public void onDelete(WatchEvent<?> watchEvent, Path path) {
                dealwith(watchEvent, path);
            }

            @Override
            public void onOverflow(WatchEvent<?> watchEvent, Path path) {
                dealwith(watchEvent, path);
            }
        });
    }

    public void start(){monitor.start();}

    public void stop(){monitor.interrupt();}
}
