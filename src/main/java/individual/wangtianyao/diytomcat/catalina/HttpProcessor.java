package individual.wangtianyao.diytomcat.catalina;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import individual.wangtianyao.diytomcat.http.Header;
import individual.wangtianyao.diytomcat.http.Request;
import individual.wangtianyao.diytomcat.http.Response;
import individual.wangtianyao.diytomcat.util.WebXMLUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HttpProcessor {
    public void execute(Socket s, Request reqs, Response resp){
        try {
            String requestString = reqs.getRequestString();
            String uri = reqs.getUri();
            // reqs类的解析方式，uri为fileName或"/"; dir存储于context中
            System.out.println("URI got from the Client Request: " + uri + "\r\n");
            System.out.println("Input Information from Explorer: \r\n" + requestString + "\r\n");


            // 解析uri，生成对应状态码
            if (uri.equals("/")) handleWelcomePage(s, resp, reqs);
            else if(uri.equals("/500")) throw new RuntimeException(
                    "This is a deliberately created 500 exception to test error exception.");
            else {
                // 显然，该分支之后应该被通用化为静态资源/文件访问

                // 在Diy Tomcat中，所有静态资源默认根目录为/webApps/ROOT;
                // 即Header.rootFolder File类实例
                String fileName = uri.substring(1, uri.length());
                File file = FileUtil.file(reqs.getContext().getDocBase(), fileName);

                if (file.exists()) {
                    // 响应timeConsuming任务
                    if (fileName.equals("wait1s.html")) {
                        try {Thread.sleep(1000);}
                        catch (Exception e) {e.printStackTrace();}
                    }
                    String suffix = FileUtil.extName(file);
                    String mimeType = WebXMLUtil.getMimeType(suffix);
                    resp.setContentType(mimeType);
                    byte[] fileContent = FileUtil.readBytes(file);
                    resp.setBody(fileContent);
                    handleResponse200(s, resp);
                }else handle404(s, uri);
            }

        } catch (Exception e) {
            handle500(s, e);
        } finally{
            try{if(!s.isClosed()) s.close();}
            catch(IOException e){e.printStackTrace();}
        }
    }

    protected void handleWelcomePage(Socket s, Response resp, Request reqs) throws IOException{
        String fileName = WebXMLUtil.getWelcomeFileName(reqs.getContext());
        //System.out.println("uri: "+fileName+"\r\nAbsolute Context"+reqs.getContext().getDocBase());

        File f = FileUtil.file(reqs.getContext().getDocBase(), fileName);
        String html = FileUtil.readUtf8String(f);
        resp.getWriter().println(html);
        OutputStream os = s.getOutputStream();
        byte[] respMessage = getResponseMessage200(resp);
        os.write(respMessage);
        os.flush();
    }

    protected void handleResponse200(Socket s, Response resp) throws IOException{
        OutputStream os = s.getOutputStream();
        byte[] respMessage = getResponseMessage200(resp);
        os.write(respMessage);
        os.flush();
    }

    private byte[] getResponseMessage200(Response resp){
        byte[] respHeader = (Header.ResponseHeader200
                + Header.getHeaderEntryLine(Header.contentType, resp.getContentType())
                + "\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] respBody = resp.getBody();
        byte[] respBytes = new byte[respHeader.length+respBody.length];
        ArrayUtil.copy(respHeader, 0, respBytes, 0, respHeader.length);
        ArrayUtil.copy(respBody, 0, respBytes, respHeader.length, respBody.length);
        return respBytes;
    }

    protected void handle404(Socket s, String uri) throws IOException{
        OutputStream os = s.getOutputStream();
        Response resp = new Response();
        String responseBody = StrUtil.format(Header.page404, uri, uri);
        String responseMessage = Header.ResponseHeader404
                + Header.getHeaderEntryLine(Header.contentType, resp.getContentType())
                + "\r\n" + responseBody;
        System.out.println(responseMessage);
        byte[] bytes = responseMessage.getBytes(StandardCharsets.UTF_8);
        os.write(bytes);
        os.flush();
    }

    protected void handle500(Socket s, Exception e){
        try {
            StackTraceElement[] stes = e.getStackTrace();
            StringBuilder sb = new StringBuilder();
            sb.append(e.toString());
            for (StackTraceElement ste : stes) {
                sb.append("\t");
                sb.append(ste.toString());
                sb.append("\r\n");
            }
            String msg = e.getMessage();
            if (msg != null && msg.length() > 20) msg = msg.substring(0, 19);

            OutputStream os = s.getOutputStream();
            Response resp = new Response();
            String responseBody = StrUtil.format(Header.page500, msg, e.toString(), sb.toString());
            String responseMessage = Header.ResponseHeader500
                    + Header.getHeaderEntryLine(Header.contentType, resp.getContentType())
                    + "\r\n" + responseBody;
            byte[] bytes = responseMessage.getBytes(StandardCharsets.UTF_8);
            os.write(bytes);
            os.flush();
        }catch (IOException e2){e2.printStackTrace();}
    }
}
