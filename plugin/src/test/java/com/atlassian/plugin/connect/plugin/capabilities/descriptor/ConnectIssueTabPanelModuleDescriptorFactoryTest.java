package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.plugin.module.jira.issuetab.IFrameIssueTab;

public class ConnectIssueTabPanelModuleDescriptorFactoryTest extends AbstractConnectTabPanelModuleDescriptorFactoryTest
{
    public ConnectIssueTabPanelModuleDescriptorFactoryTest()
    {
        super(ConnectTabPanelModuleProvider.FIELD_TO_HINTS.get(ConnectTabPanelModuleProvider.ISSUE_TAB_PANELS));
    }

}
