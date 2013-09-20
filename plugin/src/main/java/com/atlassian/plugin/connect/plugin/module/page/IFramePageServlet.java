package com.atlassian.plugin.connect.plugin.module.page;

import com.atlassian.plugin.connect.plugin.module.webfragment.UrlTemplateInstance;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlTemplateInstanceFactory;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
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
    private final IFrameRenderer iFrameRenderer;
    private final UrlTemplateInstanceFactory urlTemplateInstanceFactory;

    public IFramePageServlet(PageInfo pageInfo,
                             IFrameRenderer iFrameRenderer,
                             IFrameContext iframeContext,
                             UserManager userManager,
                             UrlTemplateInstanceFactory urlTemplateInstanceFactory)
    {
        this.iframeContext = iframeContext;
        this.iFrameRenderer = iFrameRenderer;
        this.pageInfo = pageInfo;
        this.userManager = userManager;
        this.urlTemplateInstanceFactory = urlTemplateInstanceFactory;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException
    {
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/html");

        final String remoteUsername = userManager.getRemoteUsername(req);

        final UrlTemplateInstance urlTemplateInstance =
                urlTemplateInstanceFactory.create(iframeContext.getIframePath(), req.getParameterMap(), remoteUsername);

        iFrameRenderer.renderPage(
                new IFrameContextImpl(iframeContext.getPluginKey(), urlTemplateInstance.getUrlString(),
                        iframeContext.getNamespace(), iframeContext.getIFrameParams()),
                pageInfo, req.getPathInfo(), urlTemplateInstance.getNonTemplateContextParameters(),
                remoteUsername, out);
    }


}
