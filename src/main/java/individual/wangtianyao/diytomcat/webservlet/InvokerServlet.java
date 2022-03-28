package individual.wangtianyao.diytomcat.webservlet;

import individual.wangtianyao.diytomcat.catalina.Context;
import individual.wangtianyao.diytomcat.http.Header;
import individual.wangtianyao.diytomcat.http.Request;
import individual.wangtianyao.diytomcat.http.Response;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class InvokerServlet extends HttpServlet {
    private static InvokerServlet singleton = new InvokerServlet();

    public static synchronized InvokerServlet getInstance(){
        return singleton;
    }

    private InvokerServlet(){

    }

    public void service(HttpServletRequest reqs, HttpServletResponse resp) throws IOException, ServletException{
        Request request = (Request) reqs;
        Response response = (Response) resp;
        String uri = request.getUri();
        Context context = request.getContext();
        String servletClassName = context.getServletClassName(uri);
        try {
            Class<?> clazz =  context.getWebAppClassLoader().loadClass(servletClassName); //Class.forName(servletClassName);
            Object servletObj = context.getServlet(clazz);
            System.out.println("servletClass: "+clazz);
            System.out.println("servletClass's Class Loader: "+clazz.getClassLoader());
            // 一定要注意，反射的getMethod无法得到私有方法；
            // 而HttpServlet的public service，参数为ServletRequest，而非HttpServletRequest
            clazz.getMethod("service", ServletRequest.class, ServletResponse.class)
                    .invoke(servletObj, request, response);
            if(response.getRedirectPath()==null) response.setStatus(Header.CODE_200);
            else response.setStatus(Header.CODE_302);
        }catch (Exception e){e.printStackTrace();}
    }
}
