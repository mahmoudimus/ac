package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectVersionTabPanelModuleDescriptor;

public class ConnectVersionTabPanelModuleProviderTest extends BaseConnectTabPanelModuleProviderTest
{
    public ConnectVersionTabPanelModuleProviderTest()
    {
        super(ConnectVersionTabPanelModuleDescriptor.class, ConnectTabPanelModuleProvider.VERSION_TAB_PANELS);
    }

}
