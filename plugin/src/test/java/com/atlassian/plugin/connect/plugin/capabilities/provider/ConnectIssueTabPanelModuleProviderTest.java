package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectIssueTabPanelModuleDescriptor;

public class ConnectIssueTabPanelModuleProviderTest extends BaseConnectTabPanelModuleProviderTest
{
    public ConnectIssueTabPanelModuleProviderTest()
    {
        super(ConnectIssueTabPanelModuleDescriptor.class, ConnectTabPanelModuleProvider.ISSUE_TAB_PANELS);
    }

}
