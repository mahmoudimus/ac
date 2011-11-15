package com.atlassian.labs.remoteapps.modules;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.spi.application.NonAppLinksApplicationType;
import com.atlassian.labs.remoteapps.OAuthLinkManager;
import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;
import net.oauth.OAuth;
import net.oauth.OAuthMessage;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.singletonList;

/**
 * A servlet that loads its content from a remote app's iframe
 */
public class IFramePageServlet extends HttpServlet
{
    private final TemplateRenderer templateRenderer;
    private final ApplicationLinkService applicationLinkService;
    private final PermissionManager permissionManager;
    private final NonAppLinksApplicationType applicationType;
    private final OAuthLinkManager oAuthLinkManager;
    private final WebResourceManager webResourceManager;
    private final Map<String, Object> params;
    private final String title;
    private final String iframeSrc;
    private final String decorator;

    public IFramePageServlet(TemplateRenderer templateRenderer,
                             OAuthLinkManager oAuthLinkManager,
                             ApplicationLinkService applicationLinkService,
                             PermissionManager permissionManager,
                             NonAppLinksApplicationType applicationType,
                             String title,
                             String iframeSrc,
                             String decorator,
                             WebResourceManager webResourceManager,
                             Map<String,Object> params)
    {
        this.templateRenderer = templateRenderer;
        this.applicationLinkService = applicationLinkService;
        this.permissionManager = permissionManager;
        this.applicationType = applicationType;
        this.oAuthLinkManager = oAuthLinkManager;
        this.title = title;
        this.iframeSrc = iframeSrc;
        this.decorator = decorator;
        this.webResourceManager = webResourceManager;
        this.params = params;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/html");
        ApplicationLink applicationLink = applicationLinkService.getPrimaryApplicationLink(applicationType.getClass());
        if (!permissionManager.canCurrentUserAccessRemoteApp(req, applicationLink.getId().get()))
        {
            templateRenderer.render("velocity/iframe-page-accessdenied.vm",
                ImmutableMap.<String, Object>of(
                        "title", title,
                        "decorator", decorator
                        ),
                out);
            return;
        }
        OAuthMessage message = signIframeUrl(applicationLink);

        UriBuilder uriBuilder = UriBuilder.fromUri(iframeSrc);
        for (Map.Entry<String,String> entry : message.getParameters())
        {
            uriBuilder.queryParam(entry.getKey(), entry.getValue());
        }

        webResourceManager.requireResourcesForContext("remoteapps-iframe");

        Map<String,Object> ctx = newHashMap(params);
        ctx.put("title", title);
        ctx.put("iframeSrcHtml", uriBuilder.build().toString());
        ctx.put("extraPath", req.getPathInfo() != null ? req.getPathInfo() : "");
        ctx.put("decorator", decorator);

        templateRenderer.render("velocity/iframe-page.vm", ctx, out);
    }

    private OAuthMessage signIframeUrl(ApplicationLink applicationLink)
    {
        String timestamp = System.currentTimeMillis() / 1000 + "";
        String nonce = System.nanoTime() + "";
        String signatureMethod = OAuth.RSA_SHA1;
        String oauthVersion = "1.0";

        return oAuthLinkManager.sign(applicationLink, "GET", iframeSrc, ImmutableMap.<String, List<String>>of(
                OAuth.OAUTH_SIGNATURE_METHOD, singletonList(signatureMethod), OAuth.OAUTH_NONCE, singletonList(nonce),
                OAuth.OAUTH_VERSION, singletonList(oauthVersion), OAuth.OAUTH_TIMESTAMP, singletonList(timestamp)));
    }
}
