package com.atlassian.plugin.connect.test.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectProjectTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;

public class ConnectProjectTabPanelModuleProviderTest extends BaseConnectTabPanelModuleProviderTest
{
    public ConnectProjectTabPanelModuleProviderTest()
    {
        super(ConnectProjectTabPanelModuleDescriptor.class, ConnectTabPanelModuleProvider.PROJECT_TAB_PANELS);
    }

}
