package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.versionpanel.VersionTabPanel;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectVersionTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;
import com.atlassian.plugin.connect.plugin.module.jira.versiontab.IFrameVersionTab;

/**
 * A factory to produce a ConnectVersionTabPanelModuleDescriptor from a ConnectVersionTabPanelCapabilityBean
 */
// Turning off component scanning until ACDEV-445 is resolved
public class ConnectVersionTabPanelModuleDescriptorFactory
        extends AbstractConnectTabPanelModuleDescriptorFactory<ConnectVersionTabPanelCapabilityBean, ConnectVersionTabPanelModuleDescriptor>
{
    private static final String VERSION_TAB_PAGE = "version-tab-page";
    static final String MODULE_PREFIX = "version-tab-"; // package visible for testing

    public ConnectVersionTabPanelModuleDescriptorFactory(ConnectAutowireUtil connectAutowireUtil)
    {
        super(ConnectVersionTabPanelModuleDescriptor.class, VERSION_TAB_PAGE, MODULE_PREFIX, connectAutowireUtil, IFrameVersionTab.class);
    }
}
