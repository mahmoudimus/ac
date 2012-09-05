package com.atlassian.labs.remoteapps.plugin.loader.universalbinary;

import com.atlassian.plugin.util.ClassLoaderStack;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * We are wrapping the plugins servlet in another servlet so that we can set some things up before
 * the plugins servlet is called. Currently we do the following:
 *      <ul>
 *        <li>the Threads classloader to the plugins classloader)</li>
 *        <li>wrap the request so that path info is right for the servlets</li>
 *      </ul>
 */
public class DelegatingUBServlet extends HttpServlet
{
    private final HttpServlet servlet;
    private String mountPath;
    private final ClassLoader classLoader;

    public DelegatingUBServlet(HttpServlet servlet, ClassLoader classLoader, String mountPath)
    {
        this.classLoader = classLoader;
        this.servlet = servlet;
        this.mountPath = mountPath;
    }

    @Override
    public void service(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException
    {
        ClassLoaderStack.push(classLoader);
        try
        {
            String contextPath = ((UBServletContextWrapper)servlet.getServletContext()).getContextPath();
            UBHttpRequestWrapper wrapper = new UBHttpRequestWrapper(req, contextPath, mountPath);
            servlet.service(wrapper, res);
        }
        finally
        {
            ClassLoaderStack.pop();
        }
    }

    @Override
    public void init(final ServletConfig config) throws ServletException
    {
        ClassLoaderStack.push(classLoader);
        try
        {
            servlet.init(config);
        }
        finally
        {
            ClassLoaderStack.pop();
        }
    }

    @Override
    public void destroy()
    {
        ClassLoaderStack.push(classLoader);
        try
        {
            servlet.destroy();
        }
        finally
        {
            ClassLoaderStack.pop();
        }
    }

    @Override
    public boolean equals(final Object obj)
    {
        return servlet.equals(obj);
    }

    @Override
    public String getInitParameter(final String name)
    {
        return servlet.getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames()
    {
        return servlet.getInitParameterNames();
    }

    @Override
    public ServletConfig getServletConfig()
    {
        return servlet.getServletConfig();
    }

    @Override
    public ServletContext getServletContext()
    {
        return servlet.getServletContext();
    }

    @Override
    public String getServletInfo()
    {
        return servlet.getServletInfo();
    }

    @Override
    public String getServletName()
    {
        return servlet.getServletName();
    }

    @Override
    public int hashCode()
    {
        return servlet.hashCode();
    }

    @Override
    public void init() throws ServletException
    {
        servlet.init();
    }

    @Override
    public void log(final String message, final Throwable t)
    {
        servlet.log(message, t);
    }

    @Override
    public void log(final String msg)
    {
        servlet.log(msg);
    }

    @Override
    public String toString()
    {
        return servlet.toString();
    }

}
