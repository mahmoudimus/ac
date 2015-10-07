package com.atlassian.plugin.connect.jira.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel.ConnectTabPanelModuleDescriptorFactory;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel.ConnectViewProfilePanelModuleDescriptor;
import com.atlassian.plugin.connect.jira.iframe.tabpanel.TabPanelDescriptorHints;
import com.atlassian.plugin.connect.jira.iframe.tabpanel.profile.ConnectIFrameProfileTabPanel;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.ProfileTabPanelModuleMeta;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ProfileTabPanelModuleProvider extends ConnectTabPanelModuleProvider
{

    @VisibleForTesting
    public static final TabPanelDescriptorHints HINTS = new TabPanelDescriptorHints("profile-tab-page",
            ConnectViewProfilePanelModuleDescriptor.class, ConnectIFrameProfileTabPanel.class);

    private static final ProfileTabPanelModuleMeta META = new ProfileTabPanelModuleMeta();

    @Autowired
    public ProfileTabPanelModuleProvider(ConnectTabPanelModuleDescriptorFactory descriptorFactory,
                                       IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                       IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory)
    {
        super(descriptorFactory, iFrameRenderStrategyRegistry, iFrameRenderStrategyBuilderFactory);
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<ConnectTabPanelModuleBean> modules, final Plugin theConnectPlugin, final ConnectModuleProviderContext moduleProviderContext)
    {
        TabPanelDescriptorHints hints = new TabPanelDescriptorHints("profile-tab-page",
                ConnectViewProfilePanelModuleDescriptor.class, ConnectIFrameProfileTabPanel.class);

        return provideModules(moduleProviderContext, theConnectPlugin, modules, hints);
    }

    @Override
    public String getSchemaPrefix()
    {
        return "jira";
    }

    @Override
    public ConnectModuleMeta<ConnectTabPanelModuleBean> getMeta()
    {
        return META;
    }
}
