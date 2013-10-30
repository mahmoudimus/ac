package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectVersionTabPanelCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectVersionTabPanelModuleProvider;

/**
 *
 *  Adds a tab in JIRA to the Version Summary page, supplementing existing tabs such as
 *  Summary, Issues, and Popular Issues.
 *
 */
@CapabilitySet(key = "versionTabPanels", moduleProvider = ConnectVersionTabPanelModuleProvider.class)
public class ConnectVersionTabPanelCapabilityBean extends AbstractConnectTabPanelCapabilityBean
{
    public ConnectVersionTabPanelCapabilityBean()
    {
    }

    public ConnectVersionTabPanelCapabilityBean(ConnectVersionTabPanelCapabilityBeanBuilder builder)
    {
        super(builder);
    }

    public static ConnectVersionTabPanelCapabilityBeanBuilder newVersionTabPanelBean()
    {
        return new ConnectVersionTabPanelCapabilityBeanBuilder();
    }

    public static ConnectVersionTabPanelCapabilityBeanBuilder newVersionTabPanelBean(ConnectVersionTabPanelCapabilityBean defaultBean)
    {
        return new ConnectVersionTabPanelCapabilityBeanBuilder(defaultBean);
    }

}
