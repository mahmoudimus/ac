package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectComponentTabPanelCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectComponentTabPanelModuleProvider;

/**
 * Adds a tab in JIRA to the project Browse Components page, supplementing the existing tabs such as Issues, Road Map,
 * and Change Log.
 *
 */
@CapabilitySet(key = "componentTabPanels", moduleProvider = ConnectComponentTabPanelModuleProvider.class)
public class ConnectComponentTabPanelCapabilityBean extends AbstractConnectTabPanelCapabilityBean
{
    public ConnectComponentTabPanelCapabilityBean() {}

    public ConnectComponentTabPanelCapabilityBean(ConnectComponentTabPanelCapabilityBeanBuilder builder)
    {
        super(builder);
    }

    public static ConnectComponentTabPanelCapabilityBeanBuilder newComponentTabPanelBean()
    {
        return new ConnectComponentTabPanelCapabilityBeanBuilder();
    }

    public static ConnectComponentTabPanelCapabilityBeanBuilder newComponentTabPanelBean(ConnectComponentTabPanelCapabilityBean defaultBean)
    {
        return new ConnectComponentTabPanelCapabilityBeanBuilder(defaultBean);
    }

}
