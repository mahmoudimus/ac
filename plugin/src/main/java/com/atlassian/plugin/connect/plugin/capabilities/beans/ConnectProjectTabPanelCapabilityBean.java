package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectProjectTabPanelCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectProjectTabPanelModuleProvider;

/**
 * Adds a tab in JIRA to the Browse Projects page, supplementing the built-in tabs such as Open Issues,
 *
 */
@CapabilitySet(key = "projectTabPanels", moduleProvider = ConnectProjectTabPanelModuleProvider.class)
public class ConnectProjectTabPanelCapabilityBean extends AbstractConnectTabPanelCapabilityBean
{
    public ConnectProjectTabPanelCapabilityBean() {}

    public ConnectProjectTabPanelCapabilityBean(ConnectProjectTabPanelCapabilityBeanBuilder builder)
    {
        super(builder);
    }

    public static ConnectProjectTabPanelCapabilityBeanBuilder newProjectTabPanelBean()
    {
        return new ConnectProjectTabPanelCapabilityBeanBuilder();
    }

    public static ConnectProjectTabPanelCapabilityBeanBuilder newProjectTabPanelBean(ConnectProjectTabPanelCapabilityBean defaultBean)
    {
        return new ConnectProjectTabPanelCapabilityBeanBuilder(defaultBean);
    }

}
