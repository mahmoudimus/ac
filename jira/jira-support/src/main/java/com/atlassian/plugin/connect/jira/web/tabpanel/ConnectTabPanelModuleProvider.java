package com.atlassian.plugin.connect.jira.web.tabpanel;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.web.condition.ConditionLoadingValidator;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.jira.AbstractJiraConnectModuleProvider;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public abstract class ConnectTabPanelModuleProvider extends AbstractJiraConnectModuleProvider<ConnectTabPanelModuleBean> {
    protected final PluginRetrievalService pluginRetrievalService;
    private final ConnectTabPanelModuleDescriptorFactory descriptorFactory;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final ConditionLoadingValidator conditionLoadingValidator;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;

    @Autowired
    public ConnectTabPanelModuleProvider(PluginRetrievalService pluginRetrievalService,
                                         ConnectJsonSchemaValidator schemaValidator,
                                         ConnectTabPanelModuleDescriptorFactory descriptorFactory,
                                         IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                         IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
                                         ConditionLoadingValidator conditionLoadingValidator) {
        super(pluginRetrievalService, schemaValidator);
        this.pluginRetrievalService = pluginRetrievalService;
        this.descriptorFactory = descriptorFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.conditionLoadingValidator = conditionLoadingValidator;
    }

    @Override
    public List<ConnectTabPanelModuleBean> deserializeAddonDescriptorModules(String jsonModuleListEntry, ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException {
        List<ConnectTabPanelModuleBean> tabPanels = super.deserializeAddonDescriptorModules(jsonModuleListEntry, descriptor);
        conditionLoadingValidator.validate(pluginRetrievalService.getPlugin(), descriptor, getMeta(), tabPanels);
        return tabPanels;
    }

    protected List<ModuleDescriptor> provideModules(ConnectAddonBean addonBean, List<ConnectTabPanelModuleBean> beans, TabPanelDescriptorHints hints) {
        List<ModuleDescriptor> descriptors = new ArrayList<>();
        for (ConnectTabPanelModuleBean bean : beans) {
            descriptors.add(descriptorFactory.createModuleDescriptor(addonBean,
                    pluginRetrievalService.getPlugin(), bean, hints));
            registerIframeRenderStrategy(bean, addonBean);
        }
        return descriptors;
    }

    private void registerIframeRenderStrategy(ConnectTabPanelModuleBean tabPanel, ConnectAddonBean connectAddonBean) {
        // register a render strategy for tab panels
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                .addon(connectAddonBean.getKey())
                .module(tabPanel.getKey(connectAddonBean))
                .genericBodyTemplate()
                .urlTemplate(tabPanel.getUrl())
                .conditions(tabPanel.getConditions())
                .title(tabPanel.getDisplayName())
                .build();
        iFrameRenderStrategyRegistry.register(connectAddonBean.getKey(), tabPanel.getRawKey(), renderStrategy);
    }
}
