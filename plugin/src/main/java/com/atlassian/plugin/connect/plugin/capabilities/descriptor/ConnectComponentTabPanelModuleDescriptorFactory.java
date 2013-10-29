package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectComponentTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;
import com.atlassian.plugin.connect.plugin.module.jira.componenttab.IFrameComponentTab;

/**
 * A factory to produce a ConnectComponentTabPanelModuleDescriptor from a ConnectComponentTabPanelCapabilityBean
 */
// Turning off component scanning until ACDEV-445 is resolved
public class ConnectComponentTabPanelModuleDescriptorFactory
        extends AbstractConnectTabPanelModuleDescriptorFactory<ConnectComponentTabPanelCapabilityBean, ConnectComponentTabPanelModuleDescriptor>
{
    private static final String COMPONENT_TAB_PAGE = "component-tab-page";
    static final String MODULE_PREFIX = "component-tab-"; // package visible for testing

    public ConnectComponentTabPanelModuleDescriptorFactory(ConnectAutowireUtil connectAutowireUtil)
    {
        super(ConnectComponentTabPanelModuleDescriptor.class, COMPONENT_TAB_PAGE, MODULE_PREFIX, connectAutowireUtil, IFrameComponentTab.class);
    }
}
