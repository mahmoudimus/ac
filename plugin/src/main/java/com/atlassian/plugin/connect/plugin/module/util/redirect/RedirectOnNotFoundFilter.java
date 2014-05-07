package com.atlassian.plugin.connect.plugin.module.util.redirect;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

            final StringBuffer requestURL = request.getRequestURL();
            int index = requestURL.indexOf(fromPattern);

            final String newUrl = requestURL.replace(index, index + fromPattern.length(), toPattern).toString();
            log.debug("Redirecting from {} to {}", new Object[]{request.getRequestURI(), newUrl});
            response.setStatus(HttpStatus.SC_MOVED_PERMANENTLY);
            response.addHeader(HttpHeaders.LOCATION, newUrl);
            response.getWriter().close();
        }
    }


    @Override
    public void destroy()
    {

    }
}

class RedirectingHttpServletResponseWrapper extends HttpServletResponseWrapper
{
    private PrintWriter devNullWriter;
    private ServletOutputStream devNullOutputStream;

    public RedirectingHttpServletResponseWrapper(HttpServletResponse response)
    {
        super(response);
    }

    @Override
    public void setStatus(int sc)
    {
        checkStatus(sc);
        if (!is404())
        {
            super.setStatus(sc);
        }
    }

    @Override
    public void setStatus(int sc, String sm)
    {
        checkStatus(sc);
        if (!is404())
        {
            super.setStatus(sc, sm);
        }
    }

    @Override
    public void sendError(int sc) throws IOException
    {
        checkStatus(sc);
        if (!is404())
        {
            super.sendError(sc);
        }
    }

    @Override
    public void sendError(int sc, String msg) throws IOException
    {
        checkStatus(sc);
        if (!is404())
        {
            super.sendError(sc, msg);
        }
    }

    @Override
    public PrintWriter getWriter() throws IOException
    {
        return devNullWriter != null ? devNullWriter : super.getWriter();
    }


    @Override
    public ServletOutputStream getOutputStream() throws IOException
    {
        return devNullOutputStream != null ? devNullOutputStream :
                super.getOutputStream();
    }

    boolean is404()
    {
        return devNullWriter != null;
    }

    private void checkStatus(int sc)
    {
        if (sc == HttpStatus.SC_NOT_FOUND)
        {
            devNullWriter = new PrintWriter(new CharArrayWriter());
            devNullOutputStream = new ServletOutputStream()
            {

                @Override
                public void write(int b) throws IOException
                {
                    // dev null
                }
            };
        }
    }

}