package individual.wangtianyao.diytomcat.http;

public class Header {
    public static final String ResponseHeader200="HTTP/1.1 200 OK\r\n";
    public static final String contentType="Content-Type";

    public static String getHeaderEntryLine(String headerKey, String headerValue){
        return headerKey+":"+headerValue+"\r\n";
    }
}
