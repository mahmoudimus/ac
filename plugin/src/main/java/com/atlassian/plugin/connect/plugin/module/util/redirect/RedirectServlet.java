package com.atlassian.plugin.connect.plugin.module.util.redirect;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;

import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Performs permanent redirects from the given "from" pattern to the given "to" text
 */
public final class RedirectServlet extends HttpServlet
{
    private static final Logger log = LoggerFactory.getLogger(RedirectServlet.class);
    private static final String FROM_PATTERN = "from.pattern";
    private static final String TO_TEXT = "to.text";

    private String fromPattern;
    private String toPattern;

    /**
     * Requires two servlet config parameters to be set. e.g.
     *
     * <init-param>
     *   <param-name>from.pattern</param-name>
     *   <param-value>atlassian-connect</param-value>
     * </init-param>
     * <init-param>
     *   <param-name>to.text</param-name>
     *   <param-value>ac</param-value>
     * </init-param>
     *
     * @param servletConfig
     */
    public void init(ServletConfig servletConfig) {
        fromPattern = checkNotNull(servletConfig.getInitParameter(FROM_PATTERN));
        toPattern = checkNotNull(servletConfig.getInitParameter(TO_TEXT));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        final StringBuffer requestURL = req.getRequestURL();
        int index = requestURL.indexOf(fromPattern);

        final String newUrl = requestURL.replace(index, index + fromPattern.length(), toPattern).toString();
        log.debug("Redirecting from {} to {}", new Object[] {req.getRequestURI(), newUrl});
        resp.setStatus(HttpStatus.SC_MOVED_PERMANENTLY);
        resp.addHeader(HttpHeaders.LOCATION, newUrl);
        resp.getOutputStream().close();
    }
}
