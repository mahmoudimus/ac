package com.atlassian.plugin.remotable.plugin.module.jira.projecttab;

import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.plugin.remotable.spi.PermissionDeniedException;
import com.atlassian.plugin.remotable.plugin.module.IFrameRenderer;
import com.atlassian.plugin.remotable.plugin.module.page.IFrameContext;
import com.atlassian.plugin.web.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.singletonMap;

/**
 * A tab that displays an iframe
 */
public class IFrameProjectTab implements ProjectTabPanel
{
    private final IFrameRenderer iFrameRenderer;
    private final Condition condition;
    private final IFrameContext iFrameContext;
    private static final Logger log = LoggerFactory.getLogger(IFrameProjectTab.class);

    public IFrameProjectTab(IFrameContext iFrameContext, IFrameRenderer iFrameRenderer,
            Condition condition)
    {
        this.iFrameContext = iFrameContext;
        this.iFrameRenderer = iFrameRenderer;
        this.condition = condition;
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
        Map<String,Object> context = newHashMap();
        context.put("helper", singletonMap("project", browseContext.getProject()));
        return condition == null || condition.shouldDisplay(context);
    }
}
