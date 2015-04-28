package com.atlassian.plugin.connect.spi.module;

import java.io.IOException;
import java.io.StringWriter;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.profile.ViewProfilePanel;
import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.connect.spi.PermissionDeniedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

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
        this.iFrameRenderer = checkNotNull(iFrameRenderer);
        this.iFrameContext = checkNotNull(iFrameContext);
    }

    @Override
    public void init(ViewProfilePanelModuleDescriptor viewProfilePanelModuleDescriptor)
    {
    }

    public String getHtml(User user)
    {
        StringWriter writer = new StringWriter();
        try
        {
            String remoteUsername = user != null ? user.getName() : null;
            writer.write(iFrameRenderer.render(iFrameContext, remoteUsername));
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

    public String getHtml(ApplicationUser profileUser)
    {
        User user = profileUser == null ? null : profileUser.getDirectoryUser();
        return getHtml(user);
    }
}
