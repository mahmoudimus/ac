package com.atlassian.labs.remoteapps.modules.page;

import com.atlassian.labs.remoteapps.PermissionDeniedException;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * A servlet that loads its content from a remote app's iframe
 */
public class IFramePageServlet extends HttpServlet
{
    private final TemplateRenderer templateRenderer;
    private final ApplicationLinkOperationsFactory.LinkOperations linkOps;
    private final WebResourceManager webResourceManager;
    private final Map<String, Object> params;
    private final String title;
    private final String iframePath;
    private final String decorator;

    public IFramePageServlet(TemplateRenderer templateRenderer,
                             ApplicationLinkOperationsFactory.LinkOperations linkOps,
                             String title,
                             String iframePath,
                             String decorator,
                             WebResourceManager webResourceManager,
                             Map<String,Object> params)
    {
        this.templateRenderer = templateRenderer;
        this.linkOps = linkOps;
        this.title = title;
        this.iframePath = iframePath;
        this.decorator = decorator;
        this.webResourceManager = webResourceManager;
        this.params = params; 
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/html");
        try
        {
            String signedUrl = linkOps.signGetUrl(req, iframePath);
            webResourceManager.requireResourcesForContext("remoteapps-iframe");

            Map<String,Object> ctx = newHashMap(params);
            ctx.put("title", title);
            ctx.put("iframeSrcHtml", signedUrl);
            ctx.put("extraPath", req.getPathInfo() != null ? req.getPathInfo() : "");
            ctx.put("remoteapp", linkOps.get());
            ctx.put("decorator", decorator);

            templateRenderer.render("velocity/iframe-page.vm", ctx, out);
        }
        catch (PermissionDeniedException ex)
        {
            templateRenderer.render("velocity/iframe-page-accessdenied.vm",
                ImmutableMap.<String, Object>of(
                        "title", title,
                        "decorator", decorator
                        ),
                out);
        }
    }
}
