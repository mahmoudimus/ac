package com.atlassian.plugin.remotable.host.common.service.http.bigpipe;

import javax.servlet.*;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A servlet filter that manages big pipe request ids.
 */
public class BigPipeRequestIdFilter implements Filter
{
    private final DefaultBigPipeManager bigPipeManager;

    public BigPipeRequestIdFilter(DefaultBigPipeManager bigPipeManager)
    {
        this.bigPipeManager = checkNotNull(bigPipeManager);
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
        bigPipeManager.getRequestIdAccessor().resetRequestId();
        chain.doFilter(sreq, sres);
    }

    @Override
    public void destroy()
    {
    }
}
