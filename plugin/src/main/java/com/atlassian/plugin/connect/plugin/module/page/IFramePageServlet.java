package com.atlassian.plugin.connect.plugin.module.page;

import com.atlassian.plugin.connect.plugin.module.context.ResourceNotFoundException;
import com.atlassian.plugin.connect.plugin.module.permission.UnauthorisedException;
import com.atlassian.plugin.connect.plugin.module.webfragment.InvalidContextParameterException;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlTemplateInstance;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlTemplateInstanceFactory;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

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

        try
        {
            final UrlTemplateInstance urlTemplateInstance = urlTemplateInstanceFactory.create(iframeContext.getIframePath(),
                    req.getParameterMap(), remoteUsername);

            iFrameRenderer.renderPage(
                    new IFrameContextImpl(iframeContext.getPluginKey(), urlTemplateInstance.getUrlString(),
                            iframeContext.getNamespace(), iframeContext.getIFrameParams()),
                    pageInfo, req.getPathInfo(),
                    // No longer passing the parameters here. If we need this for some reason then need to be careful they
                    // don't pass the parameter authorisation mechanism
                    ImmutableMap.<String, String[]>of()/*urlTemplateInstance.getNonTemplateContextParameters()*/,
                    remoteUsername, out);
        }
        catch (InvalidContextParameterException e)
        {
            resp.sendError(SC_BAD_REQUEST, e.getMessage());
        }
        catch (UnauthorisedException e)
        {
            e.printStackTrace();  // TODO
        }
        catch (ResourceNotFoundException e)
        {
            e.printStackTrace();  // TODO
        }
    }


}
