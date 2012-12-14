package com.atlassian.plugin.remotable.plugin.util.http.bigpipe;

import com.atlassian.plugin.remotable.spi.http.bigpipe.BigPipe;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A filter that sets the request on a thread via {@link RequestIdAccessorImpl} to allow code the ability
 * to uniquely identify a request.
 */
public final class RequestIdSettingFilter implements Filter
{
    private final BigPipe bigPipe;

    public RequestIdSettingFilter(BigPipe bigPipe)
    {
        this.bigPipe = checkNotNull(bigPipe);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
            IOException, ServletException
    {
        bigPipe.getRequestIdAccessor().resetRequestId();
        chain.doFilter(request, response);
    }

    @Override
    public void destroy()
    {
    }
}
