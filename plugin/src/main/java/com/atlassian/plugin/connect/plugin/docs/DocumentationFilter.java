package com.atlassian.plugin.connect.plugin.docs;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.atlassian.plugin.connect.plugin.ConnectPluginInfo.getPluginKey;

/**
 * Forwards requests targeting /atlassian-connect/docs/* to the bundled static documentation (which is exposed as a
 * web-resource).
 */
public class DocumentationFilter implements Filter
{
    private static final String FILTER_PREFIX = "/atlassian-connect/docs";
    private static final String DOCS_PREFIX = String.format("/download/resources/%s:docs/docs/", getPluginKey());

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain)
            throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        if (req.getServletPath().indexOf(FILTER_PREFIX) == 0) {
            String docPath = req.getServletPath().substring(FILTER_PREFIX.length());
            if (docPath.length() == 0 || docPath.endsWith("/")) {
                docPath += "index.html";
            }

            docPath = DOCS_PREFIX + docPath;

            req.getRequestDispatcher(docPath).forward(req, res);
        } else {
            filterChain.doFilter(req, res);
        }
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
