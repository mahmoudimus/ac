package com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.capabilities.provider.TabPanelDescriptorHints;

public interface ConnectTabPanelModuleDescriptorFactory
{

    ModuleDescriptor createModuleDescriptor(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin,
                                            ConnectTabPanelModuleBean bean, TabPanelDescriptorHints hints);
}