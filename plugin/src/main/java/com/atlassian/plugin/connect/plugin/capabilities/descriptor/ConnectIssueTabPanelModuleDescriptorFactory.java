package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectIssueTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;

/**
 * A factory to produce a ConnectIssueTabPanelModuleDescriptor from a ConnectIssueTabPanelCapabilityBean
 */
// Turning off component scanning until ACDEV-445 is resolved
public class ConnectIssueTabPanelModuleDescriptorFactory
        extends AbstractConnectTabPanelModuleDescriptorFactory<ConnectIssueTabPanelCapabilityBean, ConnectIssueTabPanelModuleDescriptor>
{
    private static final String ISSUE_TAB_PAGE = "issue-tab-page";
    static final String MODULE_PREFIX = "issue-tab-"; // package visible for testing

    public ConnectIssueTabPanelModuleDescriptorFactory(ConnectAutowireUtil connectAutowireUtil)
    {
        super(ConnectIssueTabPanelModuleDescriptor.class, ISSUE_TAB_PAGE, MODULE_PREFIX, connectAutowireUtil, IssueTabPanel.class);
    }
}
