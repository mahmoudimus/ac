package com.atlassian.plugin.connect.test.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.ConvertToWiredTest;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;

@ConvertToWiredTest
public class ConnectProjectTabPanelModuleDescriptorFactoryTest extends AbstractConnectTabPanelModuleDescriptorFactoryTest
{
    public ConnectProjectTabPanelModuleDescriptorFactoryTest()
    {
        super(ConnectTabPanelModuleProvider.FIELD_TO_HINTS.get(ConnectTabPanelModuleProvider.PROJECT_TAB_PANELS));
    }

}