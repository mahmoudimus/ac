package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.tabpanel.*;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.iframe.tabpanel.issue.ConnectIFrameIssueTabPanel;
import com.atlassian.plugin.connect.plugin.iframe.tabpanel.profile.ConnectIFrameProfileTabPanel;
import com.atlassian.plugin.connect.plugin.iframe.tabpanel.project.ConnectIFrameComponentTabPanel;
import com.atlassian.plugin.connect.plugin.iframe.tabpanel.project.ConnectIFrameProjectTabPanel;
import com.atlassian.plugin.connect.plugin.iframe.tabpanel.project.ConnectIFrameVersionTabPanel;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@JiraComponent
public class ConnectTabPanelModuleProvider implements ConnectModuleProvider<ConnectTabPanelModuleBean>
{
    private final ConnectTabPanelModuleDescriptorFactory descriptorFactory;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;

    public static final String COMPONENT_TAB_PANELS = "jiraComponentTabPanels";
    public static final String ISSUE_TAB_PANELS = "jiraIssueTabPanels";
    public static final String PROJECT_TAB_PANELS = "jiraProjectTabPanels";
    public static final String VERSION_TAB_PANELS = "jiraVersionTabPanels";
    public static final String PROFILE_TAB_PANELS = "jiraProfileTabPanels";

    public static final Map<String, TabPanelDescriptorHints> FIELD_TO_HINTS =
            new ImmutableMap.Builder<String, TabPanelDescriptorHints>()
                    .put(ISSUE_TAB_PANELS, new TabPanelDescriptorHints("issue-tab-page",
                            ConnectIssueTabPanelModuleDescriptor.class, ConnectIFrameIssueTabPanel.class))
                    .put(PROJECT_TAB_PANELS, new TabPanelDescriptorHints("project-tab-page",
                            ConnectProjectTabPanelModuleDescriptor.class, ConnectIFrameProjectTabPanel.class))
                    .put(COMPONENT_TAB_PANELS, new TabPanelDescriptorHints("component-tab-page",
                            ConnectComponentTabPanelModuleDescriptor.class, ConnectIFrameComponentTabPanel.class))
                    .put(VERSION_TAB_PANELS, new TabPanelDescriptorHints("version-tab-page",
                            ConnectVersionTabPanelModuleDescriptor.class, ConnectIFrameVersionTabPanel.class))
                    .put(PROFILE_TAB_PANELS, new TabPanelDescriptorHints("profile-tab-page",
                            ConnectViewProfilePanelModuleDescriptor.class, ConnectIFrameProfileTabPanel.class))
                    .build();

    @Autowired
    public ConnectTabPanelModuleProvider(ConnectTabPanelModuleDescriptorFactory descriptorFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory)
    {
        this.descriptorFactory = descriptorFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(ConnectAddonBean addon, Plugin theConnectPlugin, String jsonFieldName,
                                                 List<ConnectTabPanelModuleBean> beans)
    {
        ImmutableList.Builder<ModuleDescriptor> builder = ImmutableList.builder();

        for (ConnectTabPanelModuleBean bean : beans)
        {
            if (FIELD_TO_HINTS.containsKey(jsonFieldName))
            {
                // register a render strategy for tab panels
                IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                        .addOn(addon.getKey())
                        .module(bean.getKey(addon))
                        .genericBodyTemplate()
                        .urlTemplate(bean.getUrl())
                        .conditions(bean.getConditions())
                        .title(bean.getDisplayName())
                        .build();
                iFrameRenderStrategyRegistry.register(addon.getKey(), bean.getRawKey(), renderStrategy);

                // construct a module descriptor that JIRA will use to retrieve tab modules from
                builder.add(descriptorFactory.createModuleDescriptor(addon, theConnectPlugin, bean, FIELD_TO_HINTS.get(jsonFieldName)));
            }
        }

        return builder.build();
    }
}
