package individual.wangtianyao.diytomcat;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*
 * MiniBrowser能够给目标URL发送get请求
 * 四种方法，获取含/不含 请求头的Response
 */
public class MiniBrowser {
    public static void main(String[] args){
        String url = "http://localhost:810";
        String contentString = getContentString(url, false);
        System.out.println(contentString + "\n\n\n");

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
        byte[] result;
        try{
            // 新建一个Socket，配置url,port, connect()
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

            // 配置Request Header的首行
            String path = u.getPath();
            if(path.length()==0) path="/";
            String firstLine = "GET " + path + " HTTP/1.1\r\n";

            // 写成一个字符串，并输出到Socket；
            StringBuffer httpRequestString = new StringBuffer();
            httpRequestString.append(firstLine);
            Set<String> headers = requestHeaders.keySet();
            for(String header: headers){
                String headLine = header + ":" + requestHeaders.get(header)+"\r\n";
                httpRequestString.append(headLine);
            }

            // PrintWriter相比于PrintStream有auto flush功能
            // PrintStream相比一般的OutputStream，提供了直接打印各种java对象的能力
            PrintWriter pWriter = new PrintWriter(client.getOutputStream(), true);
            pWriter.println(httpRequestString);


            // 以下从Socket中取出
            InputStream is = client.getInputStream();
            int buffer_size = 1024;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[buffer_size];
            while(true){
                int length = is.read(buffer);
                if(length==-1) break;
                // 缓冲区从Socket的InputStream读取length长度的字节；
                // 而内部的baos只读取需要读取的字节数。
                baos.write(buffer, 0, length);
                if(length!=buffer_size) break;
            }

            result = baos.toByteArray();
            client.close();
        } catch(IOException e){
            e.printStackTrace();
            result = e.toString().getBytes(StandardCharsets.UTF_8);
        }
        return result;
    }

    public static byte[] readBytes(InputStream is, int buffer_size, boolean fully) throws IOException{
        byte[] buffer = new byte[buffer_size];
        // 见源码ByteArrayOS的std-out为一个ByteArray，可通过toXX()取出
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        while(true){
            // IS将内容读入数组（从buffer[0]开始存放）
            // 每执行一次read(),指针移向第一个未读的字节
            // 并返回实际阅读的字节数
            // 当buffer的长度为0，而is还有未读字节时，length=0
            // 当is遇到EOF，并一个字节未读时，返回-1
            // 因而length<buffer_size也说明is读到了头
            int length = is.read(buffer);
            if(length==-1) break;

            baos.write(buffer, 0, length);
            if(!fully && length!=buffer_size) break;
        }
        return baos.toByteArray();
    }

    public static byte[] readBytes(InputStream is, boolean fully) throws IOException{
        return readBytes(is, 1024, fully);
    }

}
