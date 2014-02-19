package com.atlassian.plugin.connect.test.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.ConvertToWiredTest;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;

@ConvertToWiredTest
public class ConnectVersionTabPanelModuleDescriptorFactoryTest extends AbstractConnectTabPanelModuleDescriptorFactoryTest
{
    public ConnectVersionTabPanelModuleDescriptorFactoryTest()
    {
        super(ConnectTabPanelModuleProvider.FIELD_TO_HINTS.get(ConnectTabPanelModuleProvider.VERSION_TAB_PANELS));
    }

}
