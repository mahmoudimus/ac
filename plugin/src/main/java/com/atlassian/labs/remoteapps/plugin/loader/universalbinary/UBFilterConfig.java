package com.atlassian.labs.remoteapps.plugin.loader.universalbinary;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * Instances of the UBFilterConfig are passed to plugins filter {@link javax.servlet.Filter} init() method.  It provides
 * access to the ServletContext shared by other filters and servlets in the app.
 */
public final class UBFilterConfig implements FilterConfig
{
    private final String filterName;
    private final Map<String, String> initParams;
    private final ServletContext servletContext;

    public UBFilterConfig(String filterName, Map<String, String> initParams, ServletContext servletContext)
    {
        this.filterName = filterName;
        this.initParams = initParams;
        this.servletContext = servletContext;
    }

    public String getFilterName()
    {
        return filterName;
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