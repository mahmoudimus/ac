package com.atlassian.plugin.connect.test.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.ConvertToWiredTest;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;

@ConvertToWiredTest
public class ConnectIssueTabPanelModuleDescriptorFactoryTest extends AbstractConnectTabPanelModuleDescriptorFactoryTest
{
    public ConnectIssueTabPanelModuleDescriptorFactoryTest()
    {
        super(ConnectTabPanelModuleProvider.FIELD_TO_HINTS.get(ConnectTabPanelModuleProvider.ISSUE_TAB_PANELS));
    }

}
