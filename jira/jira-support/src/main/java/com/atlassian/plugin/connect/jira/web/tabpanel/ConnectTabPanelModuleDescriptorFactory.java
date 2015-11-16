package com.atlassian.plugin.connect.jira.web.tabpanel;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.spi.lifecycle.ConnectModuleProviderContext;

public interface ConnectTabPanelModuleDescriptorFactory
{

    ModuleDescriptor createModuleDescriptor(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin,
                                            ConnectTabPanelModuleBean bean, TabPanelDescriptorHints hints);
}