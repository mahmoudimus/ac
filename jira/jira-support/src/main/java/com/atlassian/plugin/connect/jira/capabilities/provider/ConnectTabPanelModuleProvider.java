package com.atlassian.plugin.connect.jira.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel.ConnectTabPanelModuleDescriptorFactory;
import com.atlassian.plugin.connect.jira.iframe.tabpanel.TabPanelDescriptorHints;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public abstract class ConnectTabPanelModuleProvider extends AbstractJiraConnectModuleProvider<ConnectTabPanelModuleBean>
{
    protected final PluginRetrievalService pluginRetrievalService;
    private final ConnectTabPanelModuleDescriptorFactory descriptorFactory;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;

    @Autowired
    public ConnectTabPanelModuleProvider(PluginRetrievalService pluginRetrievalService,
            ConnectJsonSchemaValidator schemaValidator,
            ConnectTabPanelModuleDescriptorFactory descriptorFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory)
    {
        super(pluginRetrievalService, schemaValidator);
        this.pluginRetrievalService = pluginRetrievalService;
        this.descriptorFactory = descriptorFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
    }

    protected List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext, final Plugin theConnectPlugin, List<ConnectTabPanelModuleBean> beans, TabPanelDescriptorHints hints)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<>();
        for (ConnectTabPanelModuleBean bean : beans)
        {
            descriptors.add(descriptorFactory.createModuleDescriptor(moduleProviderContext,
                    pluginRetrievalService.getPlugin(), bean, hints));
            registerIframeRenderStrategy(bean, moduleProviderContext.getConnectAddonBean());
        }
        return descriptors;
    }

    private void registerIframeRenderStrategy(ConnectTabPanelModuleBean tabPanel, ConnectAddonBean connectAddonBean)
    {
        // register a render strategy for tab panels
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                .addOn(connectAddonBean.getKey())
                .module(tabPanel.getKey(connectAddonBean))
                .genericBodyTemplate()
                .urlTemplate(tabPanel.getUrl())
                .conditions(tabPanel.getConditions())
                .title(tabPanel.getDisplayName())
                .build();
        iFrameRenderStrategyRegistry.register(connectAddonBean.getKey(), tabPanel.getRawKey(), renderStrategy);
    }
}
