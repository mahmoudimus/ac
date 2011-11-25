package com.atlassian.labs.remoteapps.modules.page.jira;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.labs.jira4compat.api.CompatViewProfilePanel;
import com.atlassian.labs.remoteapps.PermissionDeniedException;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 *
 */
public class IFrameViewProfilePanel implements CompatViewProfilePanel
{
    private static final Logger log = LoggerFactory.getLogger(IFrameViewProfilePanel.class);
    private final TemplateRenderer templateRenderer;
    private final WebResourceManager webResourceManager;
    private final ApplicationLinkOperationsFactory.LinkOperations linkOps;
    private final Map<String, Object> params;
    private final String title;
    private final String iframePath;

    public IFrameViewProfilePanel(TemplateRenderer templateRenderer,
                                  WebResourceManager webResourceManager,
                                  ApplicationLinkOperationsFactory.LinkOperations linkOps,
                                  Map<String, Object> params,
                                  String title,
                                  String iframePath
    )
    {
        this.templateRenderer = templateRenderer;
        this.webResourceManager = webResourceManager;
        this.linkOps = linkOps;
        this.params = params;
        this.title = title;
        this.iframePath = iframePath;
    }

    @Override
    public void init(ModuleDescriptor moduleDescriptor)
    {

    }

    @Override
    public String getHtml(User user)
    {
        webResourceManager.requireResourcesForContext("remoteapps-iframe");

        StringWriter writer = new StringWriter();
        try
        {
            String signedUrl = linkOps.signGetUrl(user.getName(), iframePath);
            Map<String,Object> ctx = newHashMap(params);
            ctx.put("title", title);
            ctx.put("iframeSrcHtml", signedUrl);
            ctx.put("remoteapp", linkOps.get());
            ctx.put("extraPath", "");
            templateRenderer.render("velocity/iframe-body.vm", ctx, writer);
        }
        catch (PermissionDeniedException ex)
        {
            writer.write("Unauthorized to view this panel");
            log.warn("Unauthorized view of panel '" + title);
        }
        catch (IOException e)
        {
            writer.write("Unable to render panel: " + e.getMessage());
            log.error("Error rendering panel", e);
        }
        return writer.toString();
    }

    @Override
    public boolean showPanel(User profileUser, User currentUser)
    {
        return linkOps.canAccess(profileUser.getName());
    }
}
