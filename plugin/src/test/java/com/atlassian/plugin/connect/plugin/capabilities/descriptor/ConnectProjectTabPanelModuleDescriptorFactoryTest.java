package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.plugin.module.jira.projecttab.IFrameProjectTab;


public class ConnectProjectTabPanelModuleDescriptorFactoryTest extends AbstractConnectTabPanelModuleDescriptorFactoryTest
{
    public ConnectProjectTabPanelModuleDescriptorFactoryTest()
    {
        super(ConnectProjectTabPanelModuleDescriptor.class, IFrameProjectTab.class, ConnectTabPanelModuleProvider.FIELD_TO_HINTS.get("projectTabPanels"));
    }

}