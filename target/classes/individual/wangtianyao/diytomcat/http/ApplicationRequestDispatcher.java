package individual.wangtianyao.diytomcat.http;

import individual.wangtianyao.diytomcat.catalina.HttpProcessor;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class ApplicationRequestDispatcher implements RequestDispatcher {
    private String uri;

    public ApplicationRequestDispatcher(String uri){
        if(!uri.startsWith("/")) uri = "/"+uri;
        this.uri = uri;
    }

    @Override
    public void forward(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        Request reqs = (Request) servletRequest;
        Response resp = (Response) servletResponse;

        reqs.setUri(uri);

        HttpProcessor processor = new HttpProcessor();
        processor.execute(reqs.getSocket(), reqs, resp);
        reqs.setForwarded(true);
    }

    @Override
    public void include(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        // TODO Auto-generated method stub
    }
}
