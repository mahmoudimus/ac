package com.atlassian.plugin.connect.plugin.iframe.tabpanel.project;

import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.context.jira.JiraModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;

/**
 *
 */
public class ConnectIFrameProjectTabPanel extends AbstractConnectIFrameTabPanel<ProjectTabPanelModuleDescriptor, BrowseContext>
        implements ProjectTabPanel
{
    public ConnectIFrameProjectTabPanel(final IFrameRenderStrategy iFrameRenderStrategy, ModuleContextFilter moduleContextFilter)
    {
        super(iFrameRenderStrategy, moduleContextFilter);
    }

    @Override
    protected void populateModuleContext(final JiraModuleContextParameters moduleContext, final BrowseContext ctx)
    {
        moduleContext.addProject(ctx.getProject());
    }
}
