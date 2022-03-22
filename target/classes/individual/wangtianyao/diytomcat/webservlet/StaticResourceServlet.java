package individual.wangtianyao.diytomcat.webservlet;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import individual.wangtianyao.diytomcat.catalina.Context;
import individual.wangtianyao.diytomcat.http.Header;
import individual.wangtianyao.diytomcat.http.Request;
import individual.wangtianyao.diytomcat.http.Response;
import individual.wangtianyao.diytomcat.util.WebXMLUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class StaticResourceServlet extends HttpServlet {
    private static final StaticResourceServlet singleton = new StaticResourceServlet();

    public static synchronized StaticResourceServlet getInstance(){
        return singleton;
    }

    public void service(HttpServletRequest httpReqs, HttpServletResponse httpResp) throws IOException, ServletException {
        Request reqs = (Request) httpReqs;
        Response resp = (Response) httpResp;
        Context context = reqs.getContext();
        String uri = reqs.getUri();
        resp.setStatus(Header.CODE_200);

        switch (uri) {
            case "/":
                handleWelcomePage(resp, reqs);
                break;
            case "/hello":
                HelloWorldServlet helloServlet = new HelloWorldServlet();
                helloServlet.doGet(reqs, resp);
                break;
            case "/500":
                throw new RuntimeException(
                        "This is a deliberately created 500 exception to test error exception.");
            default:
                // 显然，该分支之后应该被通用化为静态资源/文件访问

                // 在Diy Tomcat中，所有静态资源默认根目录为/webApps/ROOT;
                // 即Header.rootFolder File类实例
                String fileName = uri.substring(1, uri.length());
                File file = FileUtil.file(reqs.getContext().getDocBase(), fileName);

                if (file.exists()) {
                    // 响应timeConsuming任务
                    if (fileName.equals("wait1s.html")) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    String suffix = FileUtil.extName(file);
                    String mimeType = WebXMLUtil.getMimeType(suffix);
                    resp.setContentType(mimeType);
                    byte[] fileContent = FileUtil.readBytes(file);
                    resp.setBody(fileContent);
                } else resp.setStatus(Header.CODE_404);
                break;
        }
    }



    protected void handleWelcomePage(Response resp, Request reqs){
        String fileName = WebXMLUtil.getWelcomeFileName(reqs.getContext());
        //System.out.println("uri: "+fileName+"\r\nAbsolute Context"+reqs.getContext().getDocBase());
        File f = FileUtil.file(reqs.getContext().getDocBase(), fileName);
        String html = FileUtil.readUtf8String(f);
        resp.getWriter().println(html);
    }

    private StaticResourceServlet(){}
}
