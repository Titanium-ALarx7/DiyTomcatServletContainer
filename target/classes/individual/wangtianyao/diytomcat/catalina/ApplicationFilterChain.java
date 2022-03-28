package individual.wangtianyao.diytomcat.catalina;

import cn.hutool.core.util.ArrayUtil;

import javax.servlet.*;
import java.io.IOException;
import java.util.List;

public class ApplicationFilterChain implements FilterChain {
    private final Filter[] filters;
    private final Servlet servlet;
    int pos=0;

    public ApplicationFilterChain(List<Filter> filterList, Servlet servlet){
        this.servlet=servlet;
        this.filters= filterList.toArray(new Filter[0]);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
        if(pos<filters.length){
            Filter filter = filters[pos++];
            filter.doFilter(servletRequest, servletResponse, this);
        }else{
            servlet.service(servletRequest, servletResponse);
        }
    }
}
