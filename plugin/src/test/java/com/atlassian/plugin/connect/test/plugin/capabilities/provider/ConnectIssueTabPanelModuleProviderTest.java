package com.atlassian.plugin.connect.test.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectIssueTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;

public class ConnectIssueTabPanelModuleProviderTest extends BaseConnectTabPanelModuleProviderTest
{
    public ConnectIssueTabPanelModuleProviderTest()
    {
        super(ConnectIssueTabPanelModuleDescriptor.class, ConnectTabPanelModuleProvider.ISSUE_TAB_PANELS);
    }

}
