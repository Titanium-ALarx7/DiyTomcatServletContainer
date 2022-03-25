package individual.wangtianyao.diytomcat.classloader;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

public class WebAppClassLoader extends URLClassLoader {
    // 用于扫描 /webapps下的 xxx/WEB-INF/ 里的 classes/ 和 lib/*.jar
    public WebAppClassLoader(String docBase, ClassLoader commonClassLoader){
        super(new URL[]{}, commonClassLoader);

        try{
            File webInfFolder = new File(docBase, "WEB-INF");
            File classesFolder = new File(webInfFolder, "classes");
            File libFolder = new File(webInfFolder, "lib");
            URL url;
            url = new URL("file:"+ classesFolder.getAbsolutePath()+"/");
            this.addURL(url);
            List<File> jarFiles = FileUtil.loopFiles(libFolder);
            for(File file: jarFiles){
                url = new URL("file:"+ file.getAbsolutePath());
                this.addURL(url);
            }
            //Arrays.stream(this.getURLs()).forEach(System.out::println);
        }catch(MalformedURLException e){e.printStackTrace();}
    }

    public void stop(){
        try{close();}
        catch(IOException e){e.printStackTrace();}
    }
}
