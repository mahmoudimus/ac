package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.versionpanel.VersionTabPanel;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectVersionTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;

/**
 * A factory to produce a ConnectVersionTabPanelModuleDescriptor from a ConnectVersionTabPanelCapabilityBean
 */
// Turning off component scanning until ACDEV-445 is resolved
public class ConnectVersionTabPanelModuleDescriptorFactory
        extends AbstractConnectTabPanelModuleDescriptorFactory<ConnectVersionTabPanelCapabilityBean, ConnectVersionTabPanelModuleDescriptor>
{
    private static final String VERSION_TAB_PAGE = "version-tab-page";

    public ConnectVersionTabPanelModuleDescriptorFactory(ConnectAutowireUtil connectAutowireUtil)
    {
        super(ConnectVersionTabPanelModuleDescriptor.class, VERSION_TAB_PAGE, connectAutowireUtil, VersionTabPanel.class);
    }
}
