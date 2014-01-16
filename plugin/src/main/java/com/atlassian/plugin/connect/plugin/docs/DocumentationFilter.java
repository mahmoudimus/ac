package com.atlassian.plugin.connect.plugin.docs;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

            if (docPath.endsWith("/"))
            {
                // append index.html if request is for a directory
                docPath += "index.html";
            }
            else if (!docPath.contains("."))
            {
                // if no file extension, assume it's a directory and redirect with a trailing slash
                res.sendRedirect(req.getRequestURI() + "/");
                return;
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
