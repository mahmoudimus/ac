package com.atlassian.plugin.remotable.host.common.service.http.bigpipe;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A servlet filter that manages big pipe request ids.
 */
public class BigPipeRequestIdFilter implements Filter
{
    private final BigPipeImpl bigPipe;

    public BigPipeRequestIdFilter(BigPipeImpl bigPipe)
    {
        this.bigPipe = checkNotNull(bigPipe);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    /**
     * Resets the BigPipe request id on each request.
     */
    @Override
    public void doFilter(ServletRequest sreq, ServletResponse sres, FilterChain chain)
        throws IOException, ServletException
    {
        bigPipe.getRequestIdAccessor().resetRequestId();
        chain.doFilter(sreq, sres);
    }

    @Override
    public void destroy()
    {
    }
}
