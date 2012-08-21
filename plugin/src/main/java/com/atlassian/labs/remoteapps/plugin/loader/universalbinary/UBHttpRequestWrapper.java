package com.atlassian.labs.remoteapps.plugin.loader.universalbinary;

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
    private final String basePath;
    private HttpServletRequest delegate;

    public UBHttpRequestWrapper(HttpServletRequest request, String servletContextPath)
    {
        super(request);
        this.delegate = request;
        this.basePath = servletContextPath;
    }

    public String getServletPath()
    {
        String servletPath = super.getServletPath();
        if (basePath != null)
        {
            servletPath += basePath;
        }
        return servletPath;
    }

    @Override
    public String getContextPath()
    {
        return basePath;
    }

    public String getPathInfo()
    {
        return delegate.getRequestURI().substring((delegate.getContextPath() + basePath).length());
    }
}
