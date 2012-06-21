package com.atlassian.labs.remoteapps.util.http.bigpipe;

import javax.servlet.*;
import java.io.IOException;

/**
 * A filter that sets the request on a thread via {@link RequestIdAccessor} to allow code the ability
 * to uniquely identify a request.
 */
public class RequestIdSettingFilter implements Filter
{
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
            IOException, ServletException
    {
        RequestIdAccessor.resetRequestId();
        chain.doFilter(request, response);
    }

    @Override
    public void destroy()
    {
    }
}
