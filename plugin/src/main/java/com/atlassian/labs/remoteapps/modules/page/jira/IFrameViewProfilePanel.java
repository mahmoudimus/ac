package com.atlassian.labs.remoteapps.modules.page.jira;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.labs.jira4compat.api.CompatViewProfilePanel;
import com.atlassian.labs.remoteapps.PermissionDeniedException;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.modules.IFrameRenderer;
import com.atlassian.labs.remoteapps.modules.page.IFrameContext;
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
    private final IFrameRenderer iFrameRenderer;
    private final IFrameContext iFrameContext;

    public IFrameViewProfilePanel(IFrameRenderer iFrameRenderer, IFrameContext iFrameContext
    )
    {
        this.iFrameRenderer = iFrameRenderer;
        this.iFrameContext = iFrameContext;
    }

    @Override
    public void init(ModuleDescriptor moduleDescriptor)
    {

    }

    @Override
    public String getHtml(User user)
    {

        StringWriter writer = new StringWriter();
        try
        {
            writer.write(iFrameRenderer.render(iFrameContext, user.getName()));
        }
        catch (PermissionDeniedException ex)
        {
            writer.write("Unauthorized to view this panel");
            log.warn("Unauthorized view of panel");
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
        return true;
    }
}
