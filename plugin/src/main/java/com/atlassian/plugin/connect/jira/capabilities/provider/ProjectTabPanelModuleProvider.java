package com.atlassian.plugin.connect.jira.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel.ConnectProjectTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel.ConnectTabPanelModuleDescriptorFactory;
import com.atlassian.plugin.connect.jira.iframe.tabpanel.TabPanelDescriptorHints;
import com.atlassian.plugin.connect.jira.iframe.tabpanel.project.ConnectIFrameProjectTabPanel;
import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ProjectTabPanelModuleProvider extends ConnectTabPanelModuleProvider
{
    public static final String DESCRIPTOR_KEY = "jiraProjectTabPanels";

    @Autowired
    public ProjectTabPanelModuleProvider(ConnectTabPanelModuleDescriptorFactory descriptorFactory,
                                         IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                         IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory)
    {
        super(descriptorFactory, iFrameRenderStrategyRegistry, iFrameRenderStrategyBuilderFactory);
    }

    @Override
    public List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext, final Plugin theConnectPlugin, List<ConnectTabPanelModuleBean> beans)
    {
        TabPanelDescriptorHints hints = new TabPanelDescriptorHints("project-tab-page",
                ConnectProjectTabPanelModuleDescriptor.class, ConnectIFrameProjectTabPanel.class);

        return provideModules(moduleProviderContext, theConnectPlugin, beans, hints);
    }

    @Override
    public String getDescriptorKey()
    {
        return DESCRIPTOR_KEY;
    }
}
