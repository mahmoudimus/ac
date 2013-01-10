package com.atlassian.plugin.remotable.host.common.service.http.bigpipe;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A servlet filter that returns bigpipe content as it is available.
 */
public class BigPipeContentFilter implements Filter
{
    private final BigPipeImpl bigPipe;

    public BigPipeContentFilter(BigPipeImpl bigPipe)
    {
        this.bigPipe = checkNotNull(bigPipe);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    /**
     * Returns available content in the form of of JSON array of maps, structured like so:
     *
     * ...for HTML content:
     * - contentId: The content id
     * - channelId: "html"
     * - content: The html to insert into the content placeholder
     *
     * ...for general string content:
     * - channelId: The channelId on which the content was promised
     * - content: The string content to deliver to the client-side channel subscriber
     */
    @Override
    public void doFilter(ServletRequest sreq, ServletResponse sres, FilterChain chain)
        throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) sreq;
        HttpServletResponse res = (HttpServletResponse) sres;
        String uri = req.getRequestURI();

        int idIndex = uri.lastIndexOf('/');
        String requestId = idIndex >= 0 && idIndex < uri.length() - 1 ? uri.substring(idIndex + 1) : null;
        if (requestId != null)
        {
            res.setStatus(200);
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            res.setHeader("Cache-Control", "no-cache");
            String result = bigPipe.waitForContent(requestId);
            res.getWriter().write(result);
        }
        else
        {
            res.sendError(404);
        }
    }

    @Override
    public void destroy()
    {
    }
}
