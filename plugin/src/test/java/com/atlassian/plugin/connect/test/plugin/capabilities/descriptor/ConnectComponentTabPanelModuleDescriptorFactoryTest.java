package com.atlassian.plugin.connect.test.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;


public class ConnectComponentTabPanelModuleDescriptorFactoryTest extends AbstractConnectTabPanelModuleDescriptorFactoryTest
{
    public ConnectComponentTabPanelModuleDescriptorFactoryTest()
    {
        super(ConnectTabPanelModuleProvider.FIELD_TO_HINTS.get(ConnectTabPanelModuleProvider.COMPONENT_TAB_PANELS));
    }

}