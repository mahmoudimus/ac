package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectProjectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;

/**
 * A factory to produce a ConnectProjectTabPanelModuleDescriptor from a ConnectProjectTabPanelCapabilityBean
 */
// Turning off component scanning until ACDEV-445 is resolved
public class ConnectProjectTabPanelModuleDescriptorFactory
        extends AbstractConnectTabPanelModuleDescriptorFactory<ConnectProjectTabPanelCapabilityBean, ConnectProjectTabPanelModuleDescriptor>
{
    public ConnectProjectTabPanelModuleDescriptorFactory(ConnectAutowireUtil connectAutowireUtil)
    {
        super(ConnectProjectTabPanelModuleDescriptor.class, "project-tab-page", connectAutowireUtil, ProjectTabPanel.class);
    }
}
