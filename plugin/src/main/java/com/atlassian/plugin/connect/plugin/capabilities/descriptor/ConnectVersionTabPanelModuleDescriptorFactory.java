package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectVersionTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;

/**
 * A factory to produce a ConnectIssueTabPanelModuleDescriptor from a ConnectIssueTabPanelCapabilityBean
 */
// Turning off component scanning until ACDEV-445 is resolved
//@Component
public class ConnectVersionTabPanelModuleDescriptorFactory
        extends AbstractConnectTabPanelModuleDescriptorFactory<ConnectVersionTabPanelCapabilityBean, ConnectVersionTabPanelModuleDescriptor>
{
    public ConnectVersionTabPanelModuleDescriptorFactory(ConnectAutowireUtil connectAutowireUtil)
    {
        super(ConnectVersionTabPanelModuleDescriptor.class, "version-tab-page", connectAutowireUtil);
    }
}
