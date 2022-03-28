package individual.wangtianyao.diytomcat.http;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;

import javax.servlet.http.Cookie;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/*
 * Response类中含有一个PrintWriter包裹的StringWriter
 * 底层使用writer写入Response，通过StringWriter保存在一个数组里。
 * @contentType默认为text/html
 */
public class Response extends BaseResponse {

    private final StringWriter stringWriter;
    private final PrintWriter writer;
    private String contentType;
    private byte[] body;
    private int status;
    private String redirectPath;
    private List<Cookie> cookies;

    @Override
    public PrintWriter getWriter() {
        return writer;
    }

    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getContentType(){return this.contentType;}

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    public String getRedirectPath() {
        return redirectPath;
    }

    public void sendRedirect(String redirectPath) {
        this.redirectPath = redirectPath;
    }

    public Response(){
        this.stringWriter= new StringWriter();
        this.writer = new PrintWriter(stringWriter);
        // set default content-type in init()
        this.contentType = "text/html";
        this.cookies = new ArrayList<>();
    }

    public String getBodyString(){
        return stringWriter.toString();
    }

    public byte[] getBody(){
        if(this.body==null) {
            String content = stringWriter.toString();
            body = content.getBytes(StandardCharsets.UTF_8);
        }
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public void addCookie(Cookie cookie) {
        this.cookies.add(cookie);
    }

    public String getCookiesHeader(){
        if(cookies==null) return "";
        String pattern = "EEE, d MMM yyyy HH:mm:ss 'GMT'";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);
        StringBuffer sb = new StringBuffer();
        for(Cookie cookie: cookies){
            sb.append("Set-Cookie:");
            System.out.println(cookie.getName()+"="+cookie.getValue()+";");
            sb.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
            if(cookie.getMaxAge()!=-1){
                sb.append("Expires=");
                Date now = new Date();
                Date expire = DateUtil.offset(now, DateField.MINUTE, cookie.getMaxAge());
                sb.append(sdf.format(expire));
                sb.append(";");
            }
            if(cookie.getPath()!=null) sb.append("Path=").append(cookie.getPath());
            sb.append("\r\n");
        }
        return sb.toString();
    }
}


