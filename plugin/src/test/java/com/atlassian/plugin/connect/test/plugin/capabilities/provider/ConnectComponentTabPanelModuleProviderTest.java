package com.atlassian.plugin.connect.test.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectComponentTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;

public class ConnectComponentTabPanelModuleProviderTest extends BaseConnectTabPanelModuleProviderTest
{
    public ConnectComponentTabPanelModuleProviderTest()
    {
        super(ConnectComponentTabPanelModuleDescriptor.class, ConnectTabPanelModuleProvider.COMPONENT_TAB_PANELS);
    }
}
