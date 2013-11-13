package com.atlassian.plugin.connect.plugin.module.permission;

import javax.annotation.Nonnull;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * A filter to restrict incoming requests unless they have been authorized via api scopes.
 * Handles requests by looking for the add-on key as a request attribute.
 */
public class ApiScopingFilter implements Filter, RequestAddOnKeyLabeler
{
    /**
     * Set by an authorisation {@link Filter} to indicate the Connect add-on that is the origin of the current request.
     */
    private static final String ADD_ON_KEY = "Add-On-Key";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
    }

    @Override
    public void destroy()
    {
    }

    @Override
    public String getAddOnKey(@Nonnull HttpServletRequest request)
    {
        return (String) request.getAttribute(ADD_ON_KEY);
    }

    @Override
    public void setAddOnKey(@Nonnull HttpServletRequest request, @Nonnull String addOnKey)
    {
        request.setAttribute(ADD_ON_KEY, addOnKey);
    }
}
