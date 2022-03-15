package individual.wangtianyao.diytomcat;

import com.sun.org.apache.xpath.internal.operations.String;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MiniBrowser {
    public static void main(String[] args) throws Exception{
        String url = "http://static.how2j.cn/diytomcat.html";
        String contentString = getContentString(url, false);
        System.out.println(contentString);

        String httpString = getHttpString(url, false);
        System.out.println(httpString);
    }

    public static String getContentString(String url){
        return getContentString(url, false);
    }

    public static String getContentString(String url, boolean gzip){
        byte[] result = getContentBytes(url, gzip);
        if(result==null) return null;
        return new String(result, StandardCharsets.UTF_8).trim();
    }

    public static byte[] getContentBytes(String url){
        return getContentBytes(url, false);
    }

    // 读取HttpBody的Bytes Array
    public static byte[] getContentBytes(String url, boolean gzip){
        byte[] response = getHttpBytes(url, gzip);
        byte[] doubleReturn = "\r\n\r\n".getBytes();
        // HttpBody紧跟在doubleReturn之后，因而检测到doubleReturn之后，确定起始位置pos，读取Body。

        int pos = -1;
        for(int i=0; i< response.length-doubleReturn.length; i++){
            byte[] temp = Arrays.copyOfRange(response, i, i+doubleReturn.length);

            if(Arrays.equals(temp, doubleReturn)) {
                pos=i;
                break;
            }
        }

        if(pos==-1) return null;
        pos += doubleReturn.length;
        return Arrays.copyOfRange(response, pos, response.length);
    }

    public static String getHttpString(String url, boolean gzip){
        byte[] bytes = getHttpBytes(url, gzip);
        return new String(bytes, StandardCharsets.UTF_8).trim();
    }

    public static String getHttpString(String url){
        return getHttpString(url, false);
    }

    public static byte[] getHttpBytes(String url, boolean gzip){
        byte[] result = null;
        try{
            URL u = new URL(url);
            Socket client = new Socket();
            int port = u.getPort();
            if(port==-1) port=80;

            InetSocketAddress inetSocketAddress = new InetSocketAddress(u.getHost(), port);
            client.connect(inetSocketAddress, 1000);

            // 将请求头格式化；
            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("Host", u.getHost()+":"+port);
            requestHeaders.put("Accept", "text/html");
            requestHeaders.put("Connection", "close");
            requestHeaders.put("User-Agent", "mini-browser/java 1.8");

            if(gzip) requestHeaders.put("Accept-Encoding", "gzip");

            String path = u.getPath();
            if(path.length()==0) path="/";
            String firstLine = "GET" + path + "HTTP/1.1\r\n";
            StringBuffer httpRequestString = new StringBuffer();



        } catch(IOException e){
            e.printStackTrace();
        }
    }


}
