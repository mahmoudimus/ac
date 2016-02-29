package com.atlassian.plugin.connect.jira.web.tabpanel;

import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.plugin.connect.api.web.context.ModuleContextFilter;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.jira.web.context.JiraModuleContextParameters;

/**
 *
 */
public class ConnectIFrameProjectTabPanel extends AbstractConnectIFrameTabPanel<ProjectTabPanelModuleDescriptor, BrowseContext>
        implements ProjectTabPanel {
    public ConnectIFrameProjectTabPanel(final IFrameRenderStrategy iFrameRenderStrategy, ModuleContextFilter moduleContextFilter) {
        super(iFrameRenderStrategy, moduleContextFilter);
    }

    @Override
    protected void populateModuleContext(final JiraModuleContextParameters moduleContext, final BrowseContext ctx) {
        moduleContext.addProject(ctx.getProject());
    }
}
