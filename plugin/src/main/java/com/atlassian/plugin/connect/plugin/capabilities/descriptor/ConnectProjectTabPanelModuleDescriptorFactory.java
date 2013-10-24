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
    private static final String PROJECT_TAB_PAGE = "project-tab-page";
    static final String MODULE_PREFIX = "project-tab-"; // package visible for testing

    public ConnectProjectTabPanelModuleDescriptorFactory(ConnectAutowireUtil connectAutowireUtil)
    {
        super(ConnectProjectTabPanelModuleDescriptor.class, PROJECT_TAB_PAGE, MODULE_PREFIX, connectAutowireUtil, ProjectTabPanel.class);
    }
}
