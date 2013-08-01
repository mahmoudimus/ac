package com.atlassian.plugin.remotable.plugin.module.jira.projecttab;

import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.plugin.remotable.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.remotable.spi.module.IFrameContext;
import com.atlassian.plugin.web.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static com.atlassian.plugin.remotable.plugin.module.jira.JiraTabConditionContext.createConditionContext;
import static com.google.common.collect.Maps.newHashMap;

/**
 * A tab that displays an iframe
 */
public class IFrameProjectTab implements ProjectTabPanel
{
    private final IFrameRendererImpl iFrameRenderer;
    private final Condition condition;
    private final IFrameContext iFrameContext;
    private static final Logger log = LoggerFactory.getLogger(IFrameProjectTab.class);

    public IFrameProjectTab(IFrameContext iFrameContext, IFrameRendererImpl iFrameRenderer,
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

            String remoteUser = getRemoteUserName(browseContext);
            writer.write(iFrameRenderer.render(iFrameContext, "", extraParams, remoteUser));
        }
         catch (IOException e)
        {
            log.error("Error rendering tab", e);
            throw new RuntimeException(e);
        }
        return writer.toString();
    }

    private String getRemoteUserName(final BrowseContext browseContext)
    {
        return browseContext.getUser() != null ? browseContext.getUser().getName() : null;
    }

    @Override
    public boolean showPanel(BrowseContext browseContext)
    {
        return condition == null || condition.shouldDisplay(createConditionContext(browseContext));
    }
}
