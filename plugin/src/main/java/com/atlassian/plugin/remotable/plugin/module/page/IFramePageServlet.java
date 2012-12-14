package com.atlassian.plugin.remotable.plugin.module.page;

import com.atlassian.plugin.remotable.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.remotable.spi.module.IFrameContext;
import com.atlassian.sal.api.user.UserManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A servlet that loads its content from a remote plugin's iframe
 */
public class IFramePageServlet extends HttpServlet
{
    private final UserManager userManager;
    private final PageInfo pageInfo;
    private final IFrameContext iframeContext;
    private final IFrameRendererImpl iFrameRenderer;

    public IFramePageServlet(PageInfo pageInfo,
            IFrameRendererImpl iFrameRenderer,
            IFrameContext iframeContext,
            UserManager userManager)
    {
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

        iFrameRenderer.renderPage(iframeContext, pageInfo, req.getPathInfo(), req.getParameterMap(),
                userManager.getRemoteUsername(req), out);

    }
}
