package com.atlassian.labs.remoteapps.plugin.loader.universalbinary;

import com.atlassian.plugin.util.ClassLoaderStack;

import javax.servlet.*;
import java.io.IOException;

/**
 *
 */
public class DelegatingUBFilter implements Filter
{
    private final Filter delegate;
    private final ClassLoader classLoader;

    public DelegatingUBFilter(Filter delegate, ClassLoader classLoader)
    {
        this.delegate = delegate;
        this.classLoader = classLoader;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        ClassLoaderStack.push(classLoader);
        try
        {
            delegate.init(filterConfig);
        }
        finally
        {
            ClassLoaderStack.pop();
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain
    ) throws IOException, ServletException
    {
        ClassLoaderStack.push(classLoader);
        try
        {
            delegate.doFilter(request, response, chain);
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
            delegate.destroy();
        }
        finally
        {
            ClassLoaderStack.pop();
        }
    }
}
