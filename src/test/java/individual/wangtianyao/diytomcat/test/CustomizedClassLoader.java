package individual.wangtianyao.diytomcat.test;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

import java.io.File;
import java.lang.reflect.Method;

public class CustomizedClassLoader extends ClassLoader{
    private File classesFolder = new File(System.getProperty("user.dir"), "b");

    public static void main(String[] ss) throws Exception{
        CustomizedClassLoader loader = new CustomizedClassLoader();

        Class<?> diyClass = loader.loadClass("cn.how2j.diytomcat.test.HOW2J");
        Object o = diyClass.getConstructor().newInstance();
        Method m = diyClass.getMethod("hello");
        m.invoke(o);
        System.out.println(diyClass.getClassLoader());
    }

    protected Class<?> findClass(String qualifiedName) throws ClassNotFoundException{
        byte[] data = loadClassData(qualifiedName);
        return defineClass(qualifiedName, data, 0, data.length);
    }

    private byte[] loadClassData(String fullQualifiedName) throws ClassNotFoundException{
        String fileName = StrUtil.replace(fullQualifiedName, ".", "/") + ".class";
        File classFile = new File(classesFolder, fileName);
        if(!classFile.exists()) throw new ClassNotFoundException(fullQualifiedName);
        return FileUtil.readBytes(classFile);
    }
}
