package com.atlassian.plugin.connect.plugin.capabilities.provider;

import java.util.List;
import java.util.Map;

import com.atlassian.fugue.Pair;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.TabPanelDescriptorHints;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.*;
import com.atlassian.plugin.connect.plugin.module.jira.componenttab.IFrameComponentTab;
import com.atlassian.plugin.connect.plugin.module.jira.issuetab.IFrameIssueTab;
import com.atlassian.plugin.connect.plugin.module.jira.projecttab.IFrameProjectTab;
import com.atlassian.plugin.connect.plugin.module.jira.versiontab.IFrameVersionTab;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectTabPanelCapabilityBean.newTabPanelBean;

/*
 * NOTE: this class is a component in the jira=plugin.xml since it contains references to JIRA specific classes 
 */
public class ConnectTabPanelModuleProvider implements ConnectModuleProvider<ConnectTabPanelCapabilityBean>
{
    private final ConnectTabPanelModuleDescriptorFactory descriptorFactory;
    
    public static final Map<String,TabPanelDescriptorHints> FIELD_TO_HINTS = new ImmutableMap.Builder<String,TabPanelDescriptorHints>()
            .put("componentTabPanels", new TabPanelDescriptorHints("component-tab-", "component-tab-page", ConnectComponentTabPanelModuleDescriptor.class, IFrameComponentTab.class))
            .put("issueTabPanels", new TabPanelDescriptorHints("issue-tab-","issue-tab-page", ConnectIssueTabPanelModuleDescriptor.class, IFrameIssueTab.class))
            .put("projectTabPanels", new TabPanelDescriptorHints("project-tab-", "project-tab-page", ConnectProjectTabPanelModuleDescriptor.class, IFrameProjectTab.class))
            .put("versionTabPanels", new TabPanelDescriptorHints("version-tab-","version-tab-page", ConnectVersionTabPanelModuleDescriptor.class, IFrameVersionTab.class))
            .build();

    public ConnectTabPanelModuleProvider(ConnectTabPanelModuleDescriptorFactory descriptorFactory) 
    {
        this.descriptorFactory = descriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, String jsonFieldName, List<ConnectTabPanelCapabilityBean> beans)
    {
        ImmutableList.Builder<ModuleDescriptor> builder = ImmutableList.builder();
        
        for(ConnectTabPanelCapabilityBean bean : beans)
        {
            if(FIELD_TO_HINTS.containsKey(jsonFieldName))
            {
                
                ConnectTabPanelCapabilityBean beanWithHints = newTabPanelBean(bean)
                        .withDescriptorHints(FIELD_TO_HINTS.get(jsonFieldName))
                        .build();
                
                builder.add(descriptorFactory.createModuleDescriptor(plugin,addonBundleContext,beanWithHints));
            }
        }
        
        return builder.build();
    }
}
