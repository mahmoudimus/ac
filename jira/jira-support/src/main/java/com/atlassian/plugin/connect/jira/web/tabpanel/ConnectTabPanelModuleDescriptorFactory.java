package com.atlassian.plugin.connect.jira.web.tabpanel;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;

public interface ConnectTabPanelModuleDescriptorFactory
{

    ModuleDescriptor createModuleDescriptor(ConnectAddonBean addonBean, Plugin plugin,
            ConnectTabPanelModuleBean bean, TabPanelDescriptorHints hints);
}
