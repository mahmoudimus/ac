package com.atlassian.plugin.connect.jira.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel.ConnectIssueTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel.ConnectProjectTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel.ConnectTabPanelModuleDescriptorFactory;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel.ConnectViewProfilePanelModuleDescriptor;
import com.atlassian.plugin.connect.jira.iframe.tabpanel.issue.ConnectIFrameIssueTabPanel;
import com.atlassian.plugin.connect.jira.iframe.tabpanel.profile.ConnectIFrameProfileTabPanel;
import com.atlassian.plugin.connect.jira.iframe.tabpanel.project.ConnectIFrameProjectTabPanel;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.jira.iframe.tabpanel.TabPanelDescriptorHints;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
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

    public static final String ISSUE_TAB_PANELS = "jiraIssueTabPanels";
    public static final String PROJECT_TAB_PANELS = "jiraProjectTabPanels";
    public static final String PROFILE_TAB_PANELS = "jiraProfileTabPanels";

    public static final Map<String, TabPanelDescriptorHints> FIELD_TO_HINTS =
            new ImmutableMap.Builder<String, TabPanelDescriptorHints>()
                    .put(ISSUE_TAB_PANELS, new TabPanelDescriptorHints("issue-tab-page",
                            ConnectIssueTabPanelModuleDescriptor.class, ConnectIFrameIssueTabPanel.class))
                    .put(PROJECT_TAB_PANELS, new TabPanelDescriptorHints("project-tab-page",
                            ConnectProjectTabPanelModuleDescriptor.class, ConnectIFrameProjectTabPanel.class))
                    .put(PROFILE_TAB_PANELS, new TabPanelDescriptorHints("profile-tab-page",
                            ConnectViewProfilePanelModuleDescriptor.class, ConnectIFrameProfileTabPanel.class))
                    .build();

    public static final Map<Class<? extends ModuleDescriptor>, String> DESCRIPTOR_TO_FIELD =
            new ImmutableMap.Builder<Class<? extends ModuleDescriptor>, String>()
                    .put(ConnectIssueTabPanelModuleDescriptor.class, ISSUE_TAB_PANELS)
                    .put(ConnectProjectTabPanelModuleDescriptor.class, PROJECT_TAB_PANELS)
                    .put(ConnectViewProfilePanelModuleDescriptor.class, PROFILE_TAB_PANELS)
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
    public List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin, String jsonFieldName,
                                                 List<ConnectTabPanelModuleBean> beans)
    {
        ImmutableList.Builder<ModuleDescriptor> builder = ImmutableList.builder();

        final ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();

        for (ConnectTabPanelModuleBean bean : beans)
        {
            if (FIELD_TO_HINTS.containsKey(jsonFieldName))
            {
                // register a render strategy for tab panels
                IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                        .addOn(connectAddonBean.getKey())
                        .module(bean.getKey(connectAddonBean))
                        .genericBodyTemplate()
                        .urlTemplate(bean.getUrl())
                        .conditions(bean.getConditions())
                        .title(bean.getDisplayName())
                        .build();
                iFrameRenderStrategyRegistry.register(connectAddonBean.getKey(), bean.getRawKey(), renderStrategy);

                // construct a module descriptor that JIRA will use to retrieve tab modules from
                builder.add(descriptorFactory.createModuleDescriptor(moduleProviderContext, theConnectPlugin, bean,
                        FIELD_TO_HINTS.get(jsonFieldName)));
            }
        }

        return builder.build();
    }
}
