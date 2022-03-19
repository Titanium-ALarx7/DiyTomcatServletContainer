package individual.wangtianyao.diytomcat.test;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.NetUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import individual.wangtianyao.diytomcat.MiniBrowser;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    @Test
    public void testStaticTestHtml(){
        String html = MiniBrowser.getContentString("http://"+ip+":"+port+"/test.html");
        System.out.println(html);


    }

    @Test
    public void testTimeConsumingTask() throws InterruptedException{
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(20, 20,
                60, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(10));


        TimeInterval timeInterval = DateUtil.timer();
        for(int i=0;i<20;i++){
            threadPool.execute(()->MiniBrowser.getContentString("http://"+ip+":"+port+"/wait1s.html"));
        }
        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.HOURS);

        long duration = timeInterval.intervalMs();
        System.out.println("Time cost for 3 request of wait1s.html: "+ duration +" Millis");
        Assert.assertTrue(duration<=10000);
    }
}
