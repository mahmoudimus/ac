package com.atlassian.labs.remoteapps.modules;

import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 */
public class IFramePageServlet extends HttpServlet
{
    private final TemplateRenderer templateRenderer;
    private final WebResourceManager webResourceManager;
    private final String title;
    private final String iframeSrc;
    private final String decorator;

    public IFramePageServlet(TemplateRenderer templateRenderer, String title, String iframeSrc, String decorator, WebResourceManager webResourceManager)
    {
        this.templateRenderer = templateRenderer;
        this.title = title;
        this.iframeSrc = iframeSrc;
        this.decorator = decorator;
        this.webResourceManager = webResourceManager;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        webResourceManager.requireResourcesForContext("remoteapps-iframe");
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/html");
        templateRenderer.render("velocity/iframe-page.vm", ImmutableMap.<String,Object>of(
                "title", title,
                "iframeSrc", iframeSrc,
                "extraPath", req.getPathInfo() != null ? req.getPathInfo() : "",
                "decorator", decorator
        ), out);
    }
}
