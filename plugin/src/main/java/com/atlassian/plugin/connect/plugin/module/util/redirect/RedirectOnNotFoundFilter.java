package com.atlassian.plugin.connect.plugin.module.util.redirect;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Servlet filter that performs permanent redirects from the given "from" pattern to the given "to" text
 * when it the response would otherwise have been a 404
 */
public class RedirectOnNotFoundFilter implements Filter
{
    private static final Logger log = LoggerFactory.getLogger(RedirectOnNotFoundFilter.class);
    private static final String FROM_PATTERN = "from.pattern";
    private static final String TO_TEXT = "to.text";

    private String fromPattern;
    private String toPattern;

    /**
     * Requires two filter config parameters to be set. e.g.
     * <p/>
     * <init-param>
     * <param-name>from.pattern</param-name>
     * <param-value>atlassian-connect</param-value>
     * </init-param>
     * <init-param>
     * <param-name>to.text</param-name>
     * <param-value>ac</param-value>
     * </init-param>
     *
     */
    public void init(FilterConfig filterConfig)
    {
        fromPattern = checkNotNull(filterConfig.getInitParameter(FROM_PATTERN));
        checkArgument(StringUtils.isNotEmpty(fromPattern));
        toPattern = checkNotNull(filterConfig.getInitParameter(TO_TEXT));
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException
    {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        RedirectingHttpServletResponseWrapper wrapper = new RedirectingHttpServletResponseWrapper(response);
        filterChain.doFilter(servletRequest, wrapper);

        if (wrapper.is404())
        {
            HttpServletRequest request = (HttpServletRequest) servletRequest;

            final String queryString = request.getQueryString();
            final StringBuffer requestURL = request.getRequestURL();

            final String newUrl = createRedirectUrl(requestURL, queryString, fromPattern, toPattern);

            log.debug("Redirecting from {} to {}", new Object[]{requestURL, newUrl});
            response.setStatus(HttpStatus.SC_MOVED_PERMANENTLY);
            response.addHeader(HttpHeaders.LOCATION, newUrl);
            response.getWriter().close();
        }
    }

    @VisibleForTesting
    public static String createRedirectUrl(StringBuffer requestURLStr, String queryString,
                                           String fromPattern, String toPattern) throws MalformedURLException
    {
        if (StringUtils.isEmpty(fromPattern))
        {
            return requestURLStr.toString();
        }

        final URL requestUrl = new URL(requestURLStr.toString());

        final String requestPath = requestUrl.getPath();


        String redirectPath = requestPath.replaceFirst(fromPattern, toPattern).replaceAll("//", "/");
        if (redirectPath.endsWith("/"))
        {
            redirectPath = redirectPath.substring(0, redirectPath.length() - 1);
        }


        /*
         * Note: fragments are not sent to the server so can ignore them
         */

        if (StringUtils.isNotEmpty(queryString))
        {
            // Despite the Javadoc implying the opposite, query params are not included in the requestURLStr
            redirectPath = redirectPath + "?" + queryString;
        }

        return new URL(requestUrl.getProtocol(), requestUrl.getHost(), requestUrl.getPort(), redirectPath).toExternalForm();
    }


    @Override
    public void destroy()
    {

    }
}

