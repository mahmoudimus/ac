package com.atlassian.labs.remoteapps.modules.page;

import com.atlassian.labs.remoteapps.PermissionDeniedException;
import com.atlassian.labs.remoteapps.modules.IFrameRenderer;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * A servlet that loads its content from a remote app's iframe
 */
public class IFramePageServlet extends HttpServlet
{
    private final TemplateRenderer templateRenderer;
    private final IFrameContext iframeContext;
    private final IFrameRenderer iFrameRenderer;
    private final String decorator;
    private final String title;

    public IFramePageServlet(TemplateRenderer templateRenderer,
                             IFrameRenderer iFrameRenderer,
                             String decorator,
                             String title,
                             IFrameContext iframeContext
                             )
    {
        this.templateRenderer = templateRenderer;
        this.iframeContext = iframeContext;
        this.iFrameRenderer = iFrameRenderer;
        this.decorator = decorator;
        this.title = title;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/html");
        try
        {

            Map<String,Object> ctx = newHashMap(iframeContext.getTemplateParams());
            ctx.put("title", title);
            ctx.put("iframeHtml", iFrameRenderer.render(iframeContext, req.getPathInfo(), req.getParameterMap()));
            ctx.put("decorator", decorator);

            templateRenderer.render("velocity/iframe-page.vm", ctx, out);
        }
        catch (PermissionDeniedException ex)
        {
            templateRenderer.render("velocity/iframe-page-accessdenied.vm",
                    ImmutableMap.<String, Object>of(
                            "title", title,
                            "decorator", decorator), out);
        }
    }
}
