package com.atlassian.plugin.remotable.kit.servlet;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import javax.inject.Provider;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 *
 */
public class LazyHttpServlet<T extends HttpServlet> extends HttpServlet
{
    private final Supplier<T> delegate;

    private LazyHttpServlet(final Provider<T> provider)
    {
        this.delegate = Suppliers.memoize(new Supplier<T>()
        {
            @Override
            public T get()
            {
                return provider.get();
            }
        });
    }

    public static <T extends HttpServlet> LazyHttpServlet<T> create(Provider<T> delegate)
    {
        return new LazyHttpServlet<T>(delegate);
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        delegate.get().service(req, resp);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException
    {
        delegate.get().service(req, res);
    }

    @Override
    public void destroy()
    {
        delegate.get().destroy();
    }

    @Override
    public String getInitParameter(String name)
    {
        return delegate.get().getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames()
    {
        return delegate.get().getInitParameterNames();
    }

    @Override
    public ServletConfig getServletConfig()
    {
        return delegate.get().getServletConfig();
    }

    @Override
    public ServletContext getServletContext()
    {
        return delegate.get().getServletContext();
    }

    @Override
    public String getServletInfo()
    {
        return delegate.get().getServletInfo();
    }

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        delegate.get().init(config);
    }

    @Override
    public void init() throws ServletException
    {
        delegate.get().init();
    }

    @Override
    public void log(String msg)
    {
        delegate.get().log(msg);
    }

    @Override
    public void log(String message, Throwable t)
    {
        delegate.get().log(message, t);
    }

    @Override
    public String getServletName()
    {
        return delegate.get().getServletName();
    }

}
