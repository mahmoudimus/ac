package com.atlassian.plugin.connect.plugin.capabilities.descriptor.tabpanel;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.provider.TabPanelDescriptorHints;

public interface ConnectTabPanelModuleDescriptorFactory
{

    ModuleDescriptor createModuleDescriptor(Plugin plugin, ConnectTabPanelModuleBean bean, TabPanelDescriptorHints hints);
}
