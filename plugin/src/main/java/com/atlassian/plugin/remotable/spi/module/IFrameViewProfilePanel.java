package com.atlassian.plugin.remotable.spi.module;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.profile.ViewProfilePanel;
import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptor;
import com.atlassian.plugin.remotable.plugin.module.IFrameRenderer;
import com.atlassian.plugin.remotable.spi.PermissionDeniedException;
import com.atlassian.plugin.remotable.plugin.module.page.IFrameContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;

/**
 * View profile panel that loads its contents from an iframe
 */
public class IFrameViewProfilePanel implements ViewProfilePanel
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
    public void init(ViewProfilePanelModuleDescriptor viewProfilePanelModuleDescriptor)
    {
    }

    @Override
    public String getHtml(User user)
    {

        StringWriter writer = new StringWriter();
        try
        {
            String remoteUser = user != null ? user.getName() : null;
            writer.write(iFrameRenderer.render(iFrameContext, remoteUser));
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

}
