package individual.wangtianyao.diytomcat.test;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import individual.wangtianyao.diytomcat.http.Header;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import individual.wangtianyao.diytomcat.MiniBrowser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MiniBrowserTest {
    private static final int port = 810;
    private static final String ip = "localhost";

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
        Assert.assertEquals(html, "\"Hello Diy Tomcat!\"");
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

    @Test
    public void testServerXMLContextLoading(){
        String html = MiniBrowser.getContentString("http://localhost:810"+"/b/Hello.html");
        System.out.println(html);
        Assert.assertTrue(html.equals("Hello Diy Tomcat from \"/b\"."));
    }

    @Test
    public void test404(){
        String html = MiniBrowser.getContentString("http://localhost:810"+"/I'dLikeA404Page");
        System.out.println(html);
        Assert.assertEquals(html, StrUtil.format(Header.page404, "/I'dLikeA404Page", "/I'dLikeA404Page"));
    }

    @Test
    public void test500(){
        String resp = MiniBrowser.getHttpString("http://localhost:810"+"/500");
        Assert.assertTrue(StrUtil.containsAny(resp, "HTTP/1.1 500 Internal Server Error"));
    }

    @Test
    public void testPicturesIndex() {
        String html = MiniBrowser.getContentString("http://localhost:810"+ "/Pictures/");
        System.out.println(html);
        Assert.assertTrue(StrUtil.containsAny(html, "Maybe you want some pics?" ));
    }

    @Test
    public void testPicJpg(){
        String html = MiniBrowser.getContentString("http://localhost:810"+ "/Pictures/pic1.jpg");
    }

    @Test
    public void testHelloServlet(){
        String html = MiniBrowser.getContentString("http://localhost:810"+ "/hello");
        Assert.assertEquals(html, "Hello DIY Tomcat! \r\n  \t\t-----From HelloWorldServlet");
    }

    @Test
    public void testj2eeHelloServlet(){
        String html = MiniBrowser.getContentString("http://localhost:810"+ "/j2ee/hello");
        Assert.assertEquals(html, "Hello DIY Tomcat! \r\n  \t\t-----From HelloWorldServlet");
    }

    @Test
    public void testPluginHelloServlet(){
        String html = MiniBrowser.getContentString("http://localhost:810"+ "/pluginHelloWeb/hello");
        System.out.println(html);
    }

    @Test
    public void testgetParam() {
        String uri = "/pluginhelloweb/param";
        String url = StrUtil.format("http://{}:{}{}", ip,port,uri);
        Map<String,Object> params = new HashMap<>();
        params.put("name","meepo");
        String html = MiniBrowser.getContentString(url, params, true);
        Assert.assertEquals(html,"Param Servlet handle GET, with parameter name: meepo");
    }

    @Test
    public void testpostParam() {
        String uri = "/pluginhelloweb/param";
        String url = StrUtil.format("http://{}:{}{}", ip,port,uri);
        Map<String,Object> params = new HashMap<>();
        params.put("name","meepo");
        String html = MiniBrowser.getContentString(url, params, false);
        Assert.assertEquals(html,"Param Servlet handle POST, with parameter name: meepo");
    }

    @Test
    public void testheaders(){
        String html = MiniBrowser.getContentString("http://localhost:810"+"/pluginhelloweb/header");
        Assert.assertEquals(html, "DIY MiniBrowser / java1.8");
    }

    @Test
    public void testSetCookie(){
        String html = MiniBrowser.getHttpString("http://localhost:810"+"/pluginhelloweb/setCookie");
        System.out.println(html);
        Assert.assertTrue(html.contains("Set-Cookie:name=PluginSet(Cookie);Expires="));
    }

    @Test
    public void testGetCookie() throws Exception{
        URL url = new URL("http://localhost:810"+"/pluginhelloweb/getCookie");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Cookie", "name=PluginTest(cookie);father=MiniBrowserTest;uuu=HHHH");
        conn.connect();
        InputStream is = conn.getInputStream();
        String html = IoUtil.read(is, "utf-8");
        System.out.println(html);
    }

    @Test
    public void testSession() throws Exception{
        String jsessionid = MiniBrowser.getContentString("http://localhost:810"+"/pluginhelloweb/setSession");
        if(jsessionid!=null) System.out.println(jsessionid=jsessionid.trim());
        String url = "http://localhost:810"+"/pluginhelloweb/getSession";
        for(int i=0;i<10;i++) {
            URL u = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setRequestProperty("Cookie", "JSESSIONID=" + jsessionid);
            conn.connect();
            InputStream is = conn.getInputStream();
            String html = IoUtil.read(is, "utf-8");
            System.out.println(html);
            conn.disconnect();
            Thread.sleep(500);
        }
    }
}
