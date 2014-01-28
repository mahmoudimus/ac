package com.atlassian.plugin.connect.test.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectVersionTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;

public class ConnectVersionTabPanelModuleProviderTest extends BaseConnectTabPanelModuleProviderTest
{
    public ConnectVersionTabPanelModuleProviderTest()
    {
        super(ConnectVersionTabPanelModuleDescriptor.class, ConnectTabPanelModuleProvider.VERSION_TAB_PANELS);
    }

}
