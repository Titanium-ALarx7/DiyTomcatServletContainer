package individual.wangtianyao.diytomcat.webservlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HelloWorldServlet extends HttpServlet {
    public void doGet(HttpServletRequest reqs, HttpServletResponse resp){
        try{
            resp.getWriter().println("Hello DIY Tomcat! \r\n  \t\t-----From HelloWorldServlet");
        }catch(IOException e){e.printStackTrace();}
    }
}
