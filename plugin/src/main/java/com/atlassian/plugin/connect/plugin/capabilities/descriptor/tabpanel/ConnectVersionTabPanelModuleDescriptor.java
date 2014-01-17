package com.atlassian.plugin.connect.plugin.capabilities.descriptor.tabpanel;

import com.atlassian.jira.plugin.versionpanel.VersionTabPanel;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanelModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.iframe.tabpanel.project.ConnectIFrameVersionTabPanel;
import com.atlassian.plugin.module.ModuleFactory;

/**
 * ModuleDescriptor for Connect version of a VersionTabPanel
 */
public class ConnectVersionTabPanelModuleDescriptor extends VersionTabPanelModuleDescriptorImpl
{
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final ModuleContextFilter moduleContextFilter;

    public ConnectVersionTabPanelModuleDescriptor(JiraAuthenticationContext authenticationContext,
            ModuleFactory moduleFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            ModuleContextFilter moduleContextFilter)
    {
        super(authenticationContext, moduleFactory);
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.moduleContextFilter = moduleContextFilter;
    }

    @Override
    public VersionTabPanel getModule()
    {
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyRegistry.getOrThrow(getPluginKey(), getKey());
        return new ConnectIFrameVersionTabPanel(renderStrategy, moduleContextFilter);
    }
}
