package com.atlassian.plugin.connect.plugin.iframe.tabpanel.project;

import com.atlassian.jira.plugin.versionpanel.BrowseVersionContext;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanel;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.context.jira.JiraModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;

/**
 *
 */
public class ConnectIFrameVersionTabPanel extends AbstractConnectIFrameTabPanel<VersionTabPanelModuleDescriptor, BrowseVersionContext>
        implements VersionTabPanel
{
    public ConnectIFrameVersionTabPanel(final IFrameRenderStrategy iFrameRenderStrategy, final ModuleContextFilter moduleContextFilter)
    {
        super(iFrameRenderStrategy, moduleContextFilter);
    }

    @Override
    protected void populateModuleContext(final JiraModuleContextParameters moduleContext, final BrowseVersionContext ctx)
    {
        moduleContext.addVersion(ctx.getVersion());
    }
}
