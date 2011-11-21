package com.atlassian.labs.remoteapps.modules.page.jira;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.spi.application.NonAppLinksApplicationType;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.labs.jira4compat.api.CompatViewProfilePanel;
import com.atlassian.labs.remoteapps.OAuthLinkManager;
import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;
import net.oauth.OAuth;
import net.oauth.OAuthMessage;
import org.netbeans.lib.cvsclient.commandLine.command.log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import static com.atlassian.labs.remoteapps.util.ServletUtils.encodeIFrameSrc;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.singletonList;

/**
 *
 */
public class IFrameViewProfilePanel implements CompatViewProfilePanel
{
    private static final Logger log = LoggerFactory.getLogger(IFrameViewProfilePanel.class);
    private final ApplicationLinkService applicationLinkService;
    private final PermissionManager permissionManager;
    private final NonAppLinksApplicationType applicationType;
    private final TemplateRenderer templateRenderer;
    private final OAuthLinkManager oAuthLinkManager;
    private final WebResourceManager webResourceManager;
    private final Map<String, Object> params;
    private final String title;
    private final String iframeSrc;

    public IFrameViewProfilePanel(PermissionManager permissionManager,
                                  TemplateRenderer templateRenderer,
                                  OAuthLinkManager oAuthLinkManager,
                                  WebResourceManager webResourceManager,
                                  Map<String, Object> params,
                                  String title,
                                  String iframeSrc,
                                  ApplicationLinkService applicationLinkService,
                                  NonAppLinksApplicationType applicationType
    )
    {
        this.permissionManager = permissionManager;
        this.applicationLinkService = applicationLinkService;
        this.applicationType = applicationType;
        this.templateRenderer = templateRenderer;
        this.oAuthLinkManager = oAuthLinkManager;
        this.webResourceManager = webResourceManager;
        this.params = params;
        this.title = title;
        this.iframeSrc = iframeSrc;
    }

    @Override
    public void init(ModuleDescriptor moduleDescriptor)
    {

    }

    @Override
    public String getHtml(User user)
    {
        ApplicationLink applicationLink = applicationLinkService.getPrimaryApplicationLink(applicationType.getClass());
        OAuthMessage message = signIframeUrl(applicationLink);

        webResourceManager.requireResourcesForContext("remoteapps-iframe");

        Map<String,Object> ctx = newHashMap(params);
        ctx.put("title", title);
        ctx.put("iframeSrcHtml", encodeIFrameSrc(iframeSrc, message));
        ctx.put("remoteapp", applicationLink);
        ctx.put("extraPath", "");

        StringWriter writer = new StringWriter();
        try
        {
            templateRenderer.render("velocity/iframe-body.vm", ctx, writer);
        }
        catch (IOException e)
        {
            writer.write("Unable to render panel: " + e.getMessage());
            log.error("Error rendering speakeasy panel", e);
        }
        return writer.toString();
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

    @Override
    public boolean showPanel(User profileUser, User currentUser)
    {
        ApplicationLink applicationLink = applicationLinkService.getPrimaryApplicationLink(applicationType.getClass());
        return permissionManager.canAccessRemoteApp(currentUser.getName(), applicationLink);
    }
}
