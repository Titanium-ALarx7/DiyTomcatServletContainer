package individual.wangtianyao.diytomcat.webservlet;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
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

public class JspServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static JspServlet instance = new JspServlet();

    public static synchronized JspServlet getInstance(){
        return instance;
    }

    private JspServlet(){}

    public void service(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
        try{
            Request reqs = (Request) request;
            Response resp = (Response) response;
            String uri = reqs.getUri();

            if(uri.equals("/")) uri = WebXMLUtil.getWelcomeFileName(reqs.getContext());
            String fileName = StrUtil.removePrefix(uri, "/");
            File file = FileUtil.file(reqs.getRealPath(fileName));

            if(file.exists()) {
                String extName = FileUtil.extName(file);
                String mimeType = WebXMLUtil.getMimeType(extName);
                resp.setContentType(mimeType);
                byte[] body = FileUtil.readBytes(file);
                resp.setBody(body);
                resp.setStatus(Header.CODE_200);
            }else resp.setStatus(Header.CODE_404);
        }catch (Exception e){throw new RuntimeException(e);}
    }
}
