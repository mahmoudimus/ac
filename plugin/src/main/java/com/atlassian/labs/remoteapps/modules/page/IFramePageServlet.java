package com.atlassian.labs.remoteapps.modules.page;

import com.atlassian.labs.remoteapps.api.PermissionDeniedException;
import com.atlassian.labs.remoteapps.modules.IFrameRenderer;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * A servlet that loads its content from a remote app's iframe
 */
public class IFramePageServlet extends HttpServlet
{
    private final TemplateRenderer templateRenderer;
    private final UserManager userManager;
    private final PageInfo pageInfo;
    private final IFrameContext iframeContext;
    private final IFrameRenderer iFrameRenderer;

    public IFramePageServlet(PageInfo pageInfo, TemplateRenderer templateRenderer,
            IFrameRenderer iFrameRenderer,
            IFrameContext iframeContext,
            UserManager userManager)
    {
        this.templateRenderer = templateRenderer;
        this.iframeContext = iframeContext;
        this.iFrameRenderer = iFrameRenderer;
        this.pageInfo = pageInfo;
        this.userManager = userManager;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
                                                                                  IOException
    {
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/html");

        String remoteUser = userManager.getRemoteUsername(req);

        try
        {
            if (!pageInfo.getCondition().shouldDisplay(Collections.<String, Object>emptyMap()))
            {
                throw new PermissionDeniedException();
            }

            Map<String, Object> ctx = newHashMap(iframeContext.getIFrameParams().getAsMap());
            ctx.put("title", pageInfo.getTitle());
            ctx.put("iframeHtml",
                    iFrameRenderer.render(iframeContext, req.getPathInfo(), req.getParameterMap(),
                            remoteUser));
            ctx.put("decorator", pageInfo.getDecorator());

            templateRenderer.render("velocity/iframe-page" + pageInfo.getTemplateSuffix() + ".vm",
                    ctx, out);
        } catch (PermissionDeniedException ex)
        {
            templateRenderer.render(
                    "velocity/iframe-page-accessdenied" + pageInfo.getTemplateSuffix() + ".vm",
                    ImmutableMap.<String, Object>of(
                            "title", pageInfo.getTitle(),
                            "decorator", pageInfo.getDecorator()), out);
        }
    }
}
