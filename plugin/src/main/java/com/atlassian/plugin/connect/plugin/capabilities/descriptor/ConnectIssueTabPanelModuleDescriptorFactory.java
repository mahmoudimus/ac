package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectIssueTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;

/**
 * A factory to produce a ConnectIssueTabPanelModuleDescriptor from a ConnectIssueTabPanelCapabilityBean
 */
// Turning off component scanning until ACDEV-445 is resolved
//@Component
public class ConnectIssueTabPanelModuleDescriptorFactory
        extends AbstractConnectTabPanelModuleDescriptorFactory<ConnectIssueTabPanelCapabilityBean, ConnectIssueTabPanelModuleDescriptor>
{
    public ConnectIssueTabPanelModuleDescriptorFactory(ConnectAutowireUtil connectAutowireUtil)
    {
        super(ConnectIssueTabPanelModuleDescriptor.class, "issue-tab-page", connectAutowireUtil);
    }
}
