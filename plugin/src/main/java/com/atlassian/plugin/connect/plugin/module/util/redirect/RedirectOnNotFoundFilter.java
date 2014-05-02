package com.atlassian.plugin.connect.plugin.module.util.redirect;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Servlet filter that performs permanent redirects from the given "from" pattern to the given "to" text
 * when it get
 */
public class RedirectOnNotFoundFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(RedirectOnNotFoundFilter.class);
    private static final String FROM_PATTERN = "from.pattern";
    private static final String TO_TEXT = "to.text";

    private String fromPattern;
    private String toPattern;

    /**
     * Requires two servlet config parameters to be set. e.g.
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
     * @param filterConfig
     */
    public void init(FilterConfig filterConfig) {
        fromPattern = checkNotNull(filterConfig.getInitParameter(FROM_PATTERN));
        toPattern = checkNotNull(filterConfig.getInitParameter(TO_TEXT));
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        log.info("****************doFilter");
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        RedirectingHttpServletResponseWrapper wrapper = new RedirectingHttpServletResponseWrapper(response);
        filterChain.doFilter(servletRequest, wrapper);
        log.info("****************after filterChain.doFilter");

        if (wrapper.is404())
        {
            log.info("****************is 404");

            HttpServletRequest request = (HttpServletRequest) servletRequest;

            final StringBuffer requestURL = request.getRequestURL();
            int index = requestURL.indexOf(fromPattern);

            final String newUrl = requestURL.replace(index, index + fromPattern.length(), toPattern).toString();
            log.debug("Redirecting from {} to {}", new Object[] {request.getRequestURI(), newUrl});
            log.info("****************Redirecting from {} to {}", new Object[] {request.getRequestURI(), newUrl});
            response.setStatus(HttpStatus.SC_MOVED_PERMANENTLY);
            response.addHeader(HttpHeaders.LOCATION, newUrl);
            response.getWriter().close();
        }
    }


    @Override
    public void destroy() {

    }
}

class RedirectingHttpServletResponseWrapper extends HttpServletResponseWrapper {
    private static final Logger log = LoggerFactory.getLogger(RedirectOnNotFoundFilter.class);
    // TODO: Do I need to hijack the output stream too?
    private CharArrayWriter devNullWriter;

    public RedirectingHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void setStatus(int sc) {
        checkStatus(sc);
        if (!is404())
        {
            super.setStatus(sc);
        }
    }

    @Override
    public void setStatus(int sc, String sm) {
        checkStatus(sc);
        if (!is404())
        {
            super.setStatus(sc, sm);
        }
    }

    private void checkStatus(int sc) {
        log.info("****************checkStatus ", sc);
        if (sc == HttpStatus.SC_NOT_FOUND)
        {
            devNullWriter = new CharArrayWriter();
        }
        log.info("****************devNullWriter ", devNullWriter);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return devNullWriter != null ? new PrintWriter(devNullWriter) : super.getWriter();
    }




    boolean is404() {
        return devNullWriter != null;
    }

}