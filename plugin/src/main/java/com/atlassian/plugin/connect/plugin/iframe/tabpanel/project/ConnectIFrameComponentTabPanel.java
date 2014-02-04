package com.atlassian.plugin.connect.plugin.iframe.tabpanel.project;

import com.atlassian.jira.plugin.componentpanel.BrowseComponentContext;
import com.atlassian.jira.plugin.componentpanel.ComponentTabPanel;
import com.atlassian.jira.plugin.componentpanel.ComponentTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.context.jira.JiraModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;

/**
 *
 */
public class ConnectIFrameComponentTabPanel extends AbstractConnectIFrameTabPanel<ComponentTabPanelModuleDescriptor, BrowseComponentContext>
        implements ComponentTabPanel
{
    public ConnectIFrameComponentTabPanel(final IFrameRenderStrategy iFrameRenderStrategy, final ModuleContextFilter moduleContextFilter)
    {
        super(iFrameRenderStrategy, moduleContextFilter);
    }

    @Override
    protected void populateModuleContext(final JiraModuleContextParameters moduleContext, final BrowseComponentContext ctx)
    {
        moduleContext.addComponent(ctx.getComponent(), ctx.getProject());
    }
}
