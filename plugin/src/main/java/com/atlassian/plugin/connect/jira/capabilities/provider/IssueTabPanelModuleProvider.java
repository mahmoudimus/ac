package com.atlassian.plugin.connect.jira.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel.ConnectIssueTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel.ConnectTabPanelModuleDescriptorFactory;
import com.atlassian.plugin.connect.jira.iframe.tabpanel.TabPanelDescriptorHints;
import com.atlassian.plugin.connect.jira.iframe.tabpanel.issue.ConnectIFrameIssueTabPanel;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.IssueTabPanelModuleMeta;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class IssueTabPanelModuleProvider extends ConnectTabPanelModuleProvider
{
    @Autowired
    public IssueTabPanelModuleProvider(ConnectTabPanelModuleDescriptorFactory descriptorFactory,
                                         IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                         IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory)
    {
        super(descriptorFactory, iFrameRenderStrategyRegistry, iFrameRenderStrategyBuilderFactory);
    }
    
    @Override
    public List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext, final Plugin theConnectPlugin, List<ConnectTabPanelModuleBean> beans)
    {
        TabPanelDescriptorHints hints = new TabPanelDescriptorHints("issue-tab-page",
                ConnectIssueTabPanelModuleDescriptor.class, ConnectIFrameIssueTabPanel.class);
        
        return provideModules(moduleProviderContext, theConnectPlugin, beans, hints);
    }

    @Override
    public String getSchemaPrefix()
    {
        return "jira";
    }

    @Override
    public ConnectModuleMeta<ConnectTabPanelModuleBean> getMeta()
    {
        return new IssueTabPanelModuleMeta();
    }
}
