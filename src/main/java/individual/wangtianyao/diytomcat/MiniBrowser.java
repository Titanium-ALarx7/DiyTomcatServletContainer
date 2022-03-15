package individual.wangtianyao.diytomcat;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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

    }

    public static byte[] getHttpBytes(String url, boolean gzip){

    }


}
