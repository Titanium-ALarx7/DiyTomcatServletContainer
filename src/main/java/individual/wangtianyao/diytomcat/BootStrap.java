package individual.wangtianyao.diytomcat;

import individual.wangtianyao.diytomcat.catalina.Server;
import individual.wangtianyao.diytomcat.classloader.CommonClassLoader;

import java.lang.reflect.Method;


public class BootStrap {
    public static void main(String[] args) throws Exception{
        CommonClassLoader loader = new CommonClassLoader();
        Thread.currentThread().setContextClassLoader(loader);

        String serverClassName = "individual.wangtianyao.diytomcat.catalina.Server";
        Class<?> serverClazz = loader.loadClass(serverClassName);

        Object serverObj = serverClazz.getConstructor().newInstance();
        Method m = serverClazz.getMethod("start");
        m.invoke(serverObj);

        System.out.println(serverClazz.getClassLoader());
    }

}
