package com.atlassian.plugin.connect.plugin.capabilities.descriptor.tabpanel;

import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.iframe.tabpanel.project.ConnectIFrameProjectTabPanel;
import com.atlassian.plugin.module.ModuleFactory;

/**
 * ModuleDescriptor for Connect project of a ProjectTabPanel
 */
public class ConnectProjectTabPanelModuleDescriptor extends ProjectTabPanelModuleDescriptorImpl
{
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final ModuleContextFilter moduleContextFilter;

    public ConnectProjectTabPanelModuleDescriptor(JiraAuthenticationContext jiraAuthenticationContext,
            ModuleFactory moduleFactory, IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            ModuleContextFilter moduleContextFilter)
    {
        super(jiraAuthenticationContext, moduleFactory);
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.moduleContextFilter = moduleContextFilter;
    }

    @Override
    public ProjectTabPanel getModule()
    {
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyRegistry.getOrThrow(getPluginKey(), getKey());
        return new ConnectIFrameProjectTabPanel(renderStrategy, moduleContextFilter);
    }

    @Override
    public String getModuleClassName()
    {
        return super.getModuleClassName();
    }
}
