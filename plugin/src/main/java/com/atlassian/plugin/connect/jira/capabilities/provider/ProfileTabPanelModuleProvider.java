package com.atlassian.plugin.connect.jira.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
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
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@JiraComponent
public class ProfileTabPanelModuleProvider extends ConnectTabPanelModuleProvider
{

    @VisibleForTesting
    public static final TabPanelDescriptorHints HINTS = new TabPanelDescriptorHints("profile-tab-page",
            ConnectViewProfilePanelModuleDescriptor.class, ConnectIFrameProfileTabPanel.class);

    private static final ProfileTabPanelModuleMeta META = new ProfileTabPanelModuleMeta();

    @Autowired
    public ProfileTabPanelModuleProvider(PluginRetrievalService pluginRetrievalService,
            ConnectJsonSchemaValidator schemaValidator,
            ConnectTabPanelModuleDescriptorFactory descriptorFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory)
    {
        super(pluginRetrievalService, schemaValidator, descriptorFactory, iFrameRenderStrategyRegistry,
                iFrameRenderStrategyBuilderFactory);
    }

    @Override
    public ConnectModuleMeta<ConnectTabPanelModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<ConnectTabPanelModuleBean> modules, final ConnectModuleProviderContext moduleProviderContext)
    {
        TabPanelDescriptorHints hints = new TabPanelDescriptorHints("profile-tab-page",
                ConnectViewProfilePanelModuleDescriptor.class, ConnectIFrameProfileTabPanel.class);

        return provideModules(moduleProviderContext, pluginRetrievalService.getPlugin(), modules, hints);
    }
}
