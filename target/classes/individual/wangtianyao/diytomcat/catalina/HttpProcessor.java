package individual.wangtianyao.diytomcat.catalina;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import individual.wangtianyao.diytomcat.http.Header;
import individual.wangtianyao.diytomcat.http.Request;
import individual.wangtianyao.diytomcat.http.Response;
import individual.wangtianyao.diytomcat.util.SessionManager;
import individual.wangtianyao.diytomcat.webservlet.InvokerServlet;
import individual.wangtianyao.diytomcat.webservlet.StaticResourceServlet;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


public class HttpProcessor {
    public void execute(Socket s, Request reqs, Response resp){
        try {
            String requestString = reqs.getRequestString();
            String uri = reqs.getUri();

            prepareSession(reqs, resp);
            // reqs类的解析方式，uri为fileName或"/"; dir存储于context中
            System.out.println("URI got from the Client Request: " + uri + "\r\n");
            System.out.println("Input Information from Explorer: \r\n" + requestString + "\r\n");

            Context context = reqs.getContext();
            String servletClassName = context.getServletClassName(uri);
            System.out.println("Context-docBase:"+ context.getDocBase() +
                    "\r\n reqs.uri: "+ uri+ "\r\n servletClassName: "+servletClassName);

            if(servletClassName!=null) InvokerServlet.getInstance().service(reqs, resp);
            else StaticResourceServlet.getInstance().service(reqs, resp);

            if(resp.getStatus()==Header.CODE_200) handleResponse200(s, reqs, resp);
            if(resp.getStatus()==Header.CODE_404) handle404(s, uri);
        } catch (Exception e) {
            handle500(s, e);
        } finally{
            try{if(!s.isClosed()) s.close();}
            catch(IOException e){e.printStackTrace();}
        }
    }

    public void prepareSession(Request reqs, Response resp){
        String jsessionid = reqs.getJSessionIdFromCookie();
        HttpSession session = SessionManager.getSession(jsessionid, reqs, resp);
        reqs.setSession(session);
    }

    private boolean isGzip(Request reqs, byte[] body, String mimeType){
        String acceptEncodings = reqs.getHeader("Accept-Encoding");
        if(!StrUtil.containsAny(acceptEncodings, "gzip")) return false;
        Connector connector = reqs.getConnector();
        if(mimeType.contains(";")) mimeType=StrUtil.subBefore(mimeType, ";",false);
        if(!connector.getCompression().equals("on")) return false;
        if(body.length < connector.getCompressionMinSize()) return false;
        String userAgents = connector.getNoCompressionUserAgents();
        String[] eachUserAgents = userAgents.split(",");
        for(String eachUserAgent: eachUserAgents){
            eachUserAgent = eachUserAgent.trim();
            String userAgent = reqs.getHeader("User-Agent");
            if(StrUtil.containsAny(userAgent, eachUserAgent)) return false;
        }
        String mimeTypes = connector.getCompressableMimeType();
        String[] eachMimeTypes = mimeTypes.split(",");
        for(String eachMimeType: eachMimeTypes){
            if(mimeType.equals(eachMimeType)) return true;
        }
        return false;
    }


    protected void handleResponse200(Socket s, Request reqs ,Response resp) throws IOException{
        OutputStream os = s.getOutputStream();
        boolean gzip = isGzip(reqs, resp.getBody(), resp.getContentType());
        byte[] respMessage = getResponseMessage200(resp, gzip);
        os.write(respMessage);
        os.flush();
    }

    protected void handle404(Socket s, String uri) throws IOException{
        Response resp = new Response();
        OutputStream os = s.getOutputStream();
        String responseBody = StrUtil.format(Header.page404, uri, uri);
        String responseMessage = Header.ResponseHeader404
                + Header.getHeaderEntryLine(Header.contentType, resp.getContentType())
                + "\r\n" + responseBody;
        byte[] bytes = responseMessage.getBytes(StandardCharsets.UTF_8);
        os.write(bytes);
        os.flush();
    }

    private byte[] getResponseMessage200(Response resp, boolean gzip){
        String responseHeader = Header.ResponseHeader200
                + Header.getHeaderEntryLine(Header.contentType, resp.getContentType())
                + resp.getCookiesHeader();
        if(gzip) responseHeader += "Content-Encoding:gzip\r\n";
        responseHeader += "\r\n";
        byte[] respHeader = responseHeader.getBytes(StandardCharsets.UTF_8);
        byte[] respBody = resp.getBody();
        if(gzip) respBody = ZipUtil.gzip(respBody);
        byte[] respBytes = new byte[respHeader.length+respBody.length];
        ArrayUtil.copy(respHeader, 0, respBytes, 0, respHeader.length);
        ArrayUtil.copy(respBody, 0, respBytes, respHeader.length, respBody.length);
        return respBytes;
    }


    protected void handle500(Socket s, Exception e){
        try {
            StackTraceElement[] stes = e.getStackTrace();
            StringBuilder sb = new StringBuilder();
            sb.append(e);
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
