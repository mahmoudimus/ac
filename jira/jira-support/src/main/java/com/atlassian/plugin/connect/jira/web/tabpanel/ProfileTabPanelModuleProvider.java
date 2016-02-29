package com.atlassian.plugin.connect.jira.web.tabpanel;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.web.condition.ConditionLoadingValidator;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.ProfileTabPanelModuleMeta;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@JiraComponent
public class ProfileTabPanelModuleProvider extends ConnectTabPanelModuleProvider {

    @VisibleForTesting
    public static final TabPanelDescriptorHints HINTS = new TabPanelDescriptorHints("profile-tab-page",
            ConnectViewProfilePanelModuleDescriptor.class, ConnectIFrameProfileTabPanel.class);

    private static final ProfileTabPanelModuleMeta META = new ProfileTabPanelModuleMeta();

    @Autowired
    public ProfileTabPanelModuleProvider(PluginRetrievalService pluginRetrievalService,
                                         ConnectJsonSchemaValidator schemaValidator,
                                         ConnectTabPanelModuleDescriptorFactory descriptorFactory,
                                         IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                         IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
                                         ConditionLoadingValidator conditionLoadingValidator) {
        super(pluginRetrievalService, schemaValidator, descriptorFactory, iFrameRenderStrategyRegistry,
                iFrameRenderStrategyBuilderFactory, conditionLoadingValidator);
    }

    @Override
    public ConnectModuleMeta<ConnectTabPanelModuleBean> getMeta() {
        return META;
    }

    @Override
    public List<ModuleDescriptor<?>> createPluginModuleDescriptors(List<ConnectTabPanelModuleBean> modules, ConnectAddonBean addon) {
        TabPanelDescriptorHints hints = new TabPanelDescriptorHints("profile-tab-page",
                ConnectViewProfilePanelModuleDescriptor.class, ConnectIFrameProfileTabPanel.class);

        return provideModules(addon, modules, hints);
    }
}
