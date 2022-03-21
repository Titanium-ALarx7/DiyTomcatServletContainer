package individual.wangtianyao.diytomcat.http;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/*
 * Response类中含有一个PrintWriter包裹的StringWriter
 * 底层使用writer写入Response，通过StringWriter保存在一个数组里。
 * @contentType默认为text/html
 */
public class Response {

    private final StringWriter stringWriter;
    private final PrintWriter writer;
    private String contentType;


    public PrintWriter getWriter() {
        return writer;
    }

    public String getContentType() {
        return contentType;
    }

    public Response(){
        this.stringWriter= new StringWriter();
        this.writer = new PrintWriter(stringWriter);
        // set default content-type in init()
        this.contentType = "text/html";
    }

    public String getBodyString(){
        return stringWriter.toString();
    }

    public byte[] getBodyBytesArray(){
        String content = stringWriter.toString();
        return content.getBytes(StandardCharsets.UTF_8);
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
