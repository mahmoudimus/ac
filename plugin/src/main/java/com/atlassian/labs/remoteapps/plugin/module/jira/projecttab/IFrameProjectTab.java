package com.atlassian.labs.remoteapps.plugin.module.jira.projecttab;

import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.labs.remoteapps.spi.PermissionDeniedException;
import com.atlassian.labs.remoteapps.plugin.module.IFrameRenderer;
import com.atlassian.labs.remoteapps.plugin.module.page.IFrameContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * A tab that displays an iframe
 */
public class IFrameProjectTab implements ProjectTabPanel
{
    private final IFrameRenderer iFrameRenderer;
    private final IFrameContext iFrameContext;
    private static final Logger log = LoggerFactory.getLogger(IFrameProjectTab.class);

    public IFrameProjectTab(IFrameContext iFrameContext, IFrameRenderer iFrameRenderer)
    {
        this.iFrameContext = iFrameContext;
        this.iFrameRenderer = iFrameRenderer;
    }

    @Override
    public void init(ProjectTabPanelModuleDescriptor projectTabPanelModuleDescriptor)
    {
    }

    @Override
    public String getHtml(BrowseContext browseContext)
    {
        StringWriter writer = new StringWriter();
        try
        {
            Map<String,String[]> extraParams = newHashMap();
            extraParams.put("ctx_project_key", new String[]{browseContext.getContextKey()});
            extraParams.put("ctx_project_id", new String[]{String.valueOf(browseContext.getProject().getId())});

            String remoteUser = browseContext.getUser() != null ? browseContext.getUser().getName()
                    : null;
            writer.write(iFrameRenderer.render(iFrameContext, "", extraParams,
                    remoteUser));
        }
        catch (PermissionDeniedException ex)
        {
            writer.write("Unauthorized to view this tab");
            log.warn("Unauthorized view of tab");
        }
        catch (IOException e)
        {
            writer.write("Unable to render tab: " + e.getMessage());
            log.error("Error rendering tab", e);
        }
        return writer.toString();
    }

    @Override
    public boolean showPanel(BrowseContext browseContext)
    {
        return true;
    }
}
