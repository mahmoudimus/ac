package com.atlassian.plugin.connect.plugin.module.page;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableMap;

/**
 * A servlet that loads its content from a remote plugin's iframe
 */
public class IFramePageServlet extends HttpServlet
{
    private final UserManager userManager;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final PageInfo pageInfo;
    private final IFrameContext iframeContext;
    private final IFrameRendererImpl iFrameRenderer;

    public IFramePageServlet(PageInfo pageInfo,
            IFrameRendererImpl iFrameRenderer,
            IFrameContext iframeContext,
            UserManager userManager,
            UrlVariableSubstitutor urlVariableSubstitutor)
    {
        this.iframeContext = iframeContext;
        this.iFrameRenderer = iFrameRenderer;
        this.pageInfo = pageInfo;
        this.userManager = userManager;
        this.urlVariableSubstitutor = urlVariableSubstitutor;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException
    {
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/html");
        String originalPath = iframeContext.getIframePath();
        String iFramePath = urlVariableSubstitutor.replace(originalPath, req.getParameterMap());

        iFrameRenderer.renderPage(
                new IFrameContextImpl(iframeContext.getPluginKey(), iFramePath, iframeContext.getNamespace(), iframeContext.getIFrameParams()),
                pageInfo, req.getPathInfo(), copyRequestContext(req, originalPath), userManager.getRemoteUsername(req), out);
    }

    private Map<String, String[]> copyRequestContext(HttpServletRequest req, String path)
    {
        Set<String> variablesUsedInPath = urlVariableSubstitutor.getContextVariables(path);
        ImmutableMap.Builder<String, String[]> builder = ImmutableMap.builder();
        final Set<Map.Entry<String, String[]>> requestParameters = (Set<Map.Entry<String, String[]>>) req.getParameterMap().entrySet();
        for (Map.Entry<String, String[]> entry : requestParameters)
        {
            // copy only these context parameters which aren't already a part of URL.
            if (!variablesUsedInPath.contains(entry.getKey()))
            {
                builder.put(entry.getKey(), entry.getValue());
            }
        }
        return builder.build();
    }

}
