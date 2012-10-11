package com.atlassian.plugin.remotable.plugin.loader.universalbinary;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * Instances of the UBServletConfig are passed to plugins servlet {@link javax.servlet.Servlet} init() method.  It provides
 * access to the ServletContext shared by other servlets in the app.
 */
public final class UBServletConfig implements ServletConfig
{
    private final String servletName;
    private final Map<String, String> initParams;
    private final ServletContext servletContext;

    public UBServletConfig(String servletName, Map<String,String> initParams, ServletContext servletContext)
    {
        this.servletName = servletName;
        this.initParams = initParams;
        this.servletContext = servletContext;
    }

    public String getServletName()
    {
        return servletName;
    }

    public ServletContext getServletContext()
    {
        return servletContext;
    }

    public String getInitParameter(String s)
    {
        return initParams.get(s);
    }

    public Enumeration getInitParameterNames()
    {
        return Collections.enumeration(initParams.keySet());
    }
}