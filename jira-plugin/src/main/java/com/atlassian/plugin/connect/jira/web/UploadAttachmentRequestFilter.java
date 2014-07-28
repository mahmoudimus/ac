package com.atlassian.plugin.connect.jira.web;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class UploadAttachmentRequestFilter implements Filter
{
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException
    {

    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain)
            throws IOException, ServletException
    {
        // TODO if attachment, add X-Atlassian-Token: nocheck header to servlet request.
        // TODO add func test in this plugin
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy()
    {

    }
}
