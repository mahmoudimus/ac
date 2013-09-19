package com.atlassian.plugin.connect.plugin.module.page;

import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlTemplateInstance;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
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
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final ContextMapURLSerializer contextMapURLSerializer;
    private final PageInfo pageInfo;
    private final IFrameContext iframeContext;
    private final IFrameRenderer iFrameRenderer;

    public IFramePageServlet(PageInfo pageInfo,
            IFrameRenderer iFrameRenderer,
            IFrameContext iframeContext,
            UserManager userManager,
            UrlVariableSubstitutor urlVariableSubstitutor,
            ContextMapURLSerializer contextMapURLSerializer)
    {
        this.iframeContext = iframeContext;
        this.iFrameRenderer = iFrameRenderer;
        this.pageInfo = pageInfo;
        this.userManager = userManager;
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.contextMapURLSerializer = contextMapURLSerializer;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException
    {
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/html");

        final UrlTemplateInstance urlTemplateInstance = new UrlTemplateInstance(iframeContext.getIframePath(),
                req.getParameterMap(), urlVariableSubstitutor, contextMapURLSerializer, null);

        iFrameRenderer.renderPage(
                new IFrameContextImpl(iframeContext.getPluginKey(), urlTemplateInstance.getUrlString(),
                        iframeContext.getNamespace(), iframeContext.getIFrameParams()),
                pageInfo, req.getPathInfo(), urlTemplateInstance.getNonTemplateContextParameters(),
                userManager.getRemoteUsername(req), out);
    }


}
