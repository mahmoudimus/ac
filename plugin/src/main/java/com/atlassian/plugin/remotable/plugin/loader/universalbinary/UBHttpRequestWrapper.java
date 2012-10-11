package com.atlassian.plugin.remotable.plugin.loader.universalbinary;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * A request wrapper for requests bound for servlets declared in plugins.  Does the necessary path
 * munging for requests so that they look like they are
 * <p>
 * Also wraps the HttpSession in order to work around the Weblogic Session Attribute serialization problem (see PLUG-515)
 */
public class UBHttpRequestWrapper extends HttpServletRequestWrapper
{
    private final String contextPath;
    private String servletPath;

    public UBHttpRequestWrapper(HttpServletRequest request, String contextPath, String servletPath)
    {
        super(request);
        this.contextPath = contextPath;
        this.servletPath = servletPath;
    }

    @Override
    public String getContextPath()
    {
        return contextPath;
    }

    @Override
    public String getServletPath()
    {
        return servletPath;
    }

    @Override
    public String getPathInfo()
    {
        String uri = getRequestURI();
        int start = uri.indexOf(getContextPath());
        return getRequestURI().substring(start + contextPath.length() + servletPath.length());
    }
}
