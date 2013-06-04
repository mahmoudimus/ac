package com.atlassian.plugin.remotable.plugin.loader.universalbinary;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.remotable.host.common.descriptor.DocumentationUrlRedirect;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 */
public class UBDocumentationRedirectFilter implements Filter
{
    private final Plugin plugin;

    public UBDocumentationRedirectFilter(Plugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException
    {
        if (!DocumentationUrlRedirect.redirect(plugin.getPluginInformation().getParameters(), (HttpServletResponse) response))
        {
            chain.doFilter(request, response);
        }

    }

    @Override
    public void destroy()
    {
    }
}
