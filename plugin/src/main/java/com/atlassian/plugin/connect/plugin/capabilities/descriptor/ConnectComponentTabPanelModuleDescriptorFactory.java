package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.componentpanel.ComponentTabPanel;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectComponentTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;

/**
 * A factory to produce a ConnectComponentTabPanelModuleDescriptor from a ConnectComponentTabPanelCapabilityBean
 */
// Turning off component scanning until ACDEV-445 is resolved
public class ConnectComponentTabPanelModuleDescriptorFactory
        extends AbstractConnectTabPanelModuleDescriptorFactory<ConnectComponentTabPanelCapabilityBean, ConnectComponentTabPanelModuleDescriptor>
{
    public ConnectComponentTabPanelModuleDescriptorFactory(ConnectAutowireUtil connectAutowireUtil)
    {
        super(ConnectComponentTabPanelModuleDescriptor.class, "component-tab-page", connectAutowireUtil, ComponentTabPanel.class);
    }
}
