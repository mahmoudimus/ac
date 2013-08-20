package com.atlassian.plugin.connect.spi.http.bigpipe;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.api.service.http.bigpipe.ConsumableBigPipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A servlet filter that returns bigpipe content as it is available.
 */
public class BigPipeContentFilter implements Filter
{
    private static final Pattern URI_PATTERN = Pattern.compile("/bigpipe/request/([0-9a-fA-F]+)(/[0-9]+)?$");
    private static final Logger log = LoggerFactory.getLogger(BigPipeContentFilter.class);

    private final DefaultBigPipeManager bigPipeManager;

    public BigPipeContentFilter(DefaultBigPipeManager bigPipeManager)
    {
        this.bigPipeManager = checkNotNull(bigPipeManager);
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

        log.debug("Incoming big pipe content request on {}", Thread.currentThread().getId());
        Matcher matcher = URI_PATTERN.matcher(uri);
        if (matcher.find())
        {
            String requestId = matcher.group(1);
            Option<ConsumableBigPipe> bigPipeOption = bigPipeManager.getConsumableBigPipe(requestId);
            if (!bigPipeOption.isEmpty())
            {
                res.setStatus(HttpServletResponse.SC_OK);
                res.setContentType("application/json");
                res.setCharacterEncoding("UTF-8");
                res.setHeader("Cache-Control", "no-cache");
                String result = bigPipeOption.get().waitForContent();
                res.getWriter().write(result);
            }
            else
            {
                log.warn("Big pipe is empty, returning 404 on {}", Thread.currentThread().getId());
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
        else
        {
            log.warn("ID pattern not matched for big pipe content request on {}", Thread.currentThread().getId());
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        log.debug("Big pipe content request sent on {}", Thread.currentThread().getId());
    }

    @Override
    public void destroy()
    {
    }
}
