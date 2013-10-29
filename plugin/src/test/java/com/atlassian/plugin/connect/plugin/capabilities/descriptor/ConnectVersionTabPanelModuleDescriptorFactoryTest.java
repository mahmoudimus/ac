package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.plugin.module.jira.versiontab.IFrameVersionTab;


public class ConnectVersionTabPanelModuleDescriptorFactoryTest extends AbstractConnectTabPanelModuleDescriptorFactoryTest
{
    public ConnectVersionTabPanelModuleDescriptorFactoryTest()
    {
        super(ConnectVersionTabPanelModuleDescriptor.class, IFrameVersionTab.class, ConnectTabPanelModuleProvider.FIELD_TO_HINTS.get("versionTabPanels"));
    }

}
