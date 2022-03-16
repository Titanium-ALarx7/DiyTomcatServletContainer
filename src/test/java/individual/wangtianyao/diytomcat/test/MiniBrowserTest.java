package individual.wangtianyao.diytomcat.test;

import cn.hutool.core.util.NetUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import individual.wangtianyao.diytomcat.MiniBrowser;

public class MiniBrowserTest {
    private static int port = 810;
    private static String ip = "localhost";

    @BeforeClass
    public static void beforeClass(){
        // 在所有测试开始前，pre-test Server是否正常启动
        if(NetUtil.isUsableLocalPort(port)) {
            System.err.println("请先启动 位于端口: " +port+ " 的diy tomcat，否则无法进行单元测试");
            System.exit(1);
        }else {
            System.out.println("检测到Diy Tomcat Server已经正常启动，可进行单元测试");
        }
    }

    @Test
    public void testHelloTomcat(){
        String html = MiniBrowser.getContentString("http://"+ip+":"+port+"/");
        System.out.println(html);
        Assert.assertEquals(html, "Hello Diy Tomcat!");
    }
}
