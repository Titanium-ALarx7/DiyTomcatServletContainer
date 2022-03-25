package individual.wangtianyao.diytomcat.http;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class StandardServletConfig implements ServletConfig {
    private ServletContext servletContext;
    private Map<String, String> initParameters;
    private String servletName;

    public StandardServletConfig(ServletContext sc, String servletName, Map<String, String> iP){
        this.servletContext=sc;
        this.servletName=servletName;
        this.initParameters = iP;
        if(this.initParameters==null) this.initParameters=new HashMap<>();
    }

    @Override
    public String getServletName() {
        return this.servletName;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public String getInitParameter(String s) {
        return this.initParameters.get(s);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(this.initParameters.keySet());
    }
}
