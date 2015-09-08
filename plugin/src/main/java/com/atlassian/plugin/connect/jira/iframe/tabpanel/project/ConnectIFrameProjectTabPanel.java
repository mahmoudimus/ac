package com.atlassian.plugin.connect.jira.iframe.tabpanel.project;

import com.atlassian.jira.compatibility.bridge.project.browse.BrowseContextHelperBridge;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.plugin.connect.api.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.jira.iframe.context.JiraModuleContextParameters;

/**
 *
 */
public class ConnectIFrameProjectTabPanel extends AbstractConnectIFrameTabPanel<ProjectTabPanelModuleDescriptor, BrowseContext>
        implements ProjectTabPanel
{
    public ConnectIFrameProjectTabPanel(final IFrameRenderStrategy iFrameRenderStrategy, ModuleContextFilter moduleContextFilter, BrowseContextHelperBridge browseContextHelper)
    {
        super(iFrameRenderStrategy, moduleContextFilter, browseContextHelper);
    }

    @Override
    protected void populateModuleContext(final JiraModuleContextParameters moduleContext, final BrowseContext ctx)
    {
        moduleContext.addProject(ctx.getProject());
    }
}
