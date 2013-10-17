package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectIssueTabPanelCapabilityBean;

/**
 * Builder for a ConnectIssueTabPanelCapabilityBean
 */
public class ConnectIssueTabPanelCapabilityBeanBuilder extends AbstractConnectTabPanelCapabilityBeanBuilder
{

    public ConnectIssueTabPanelCapabilityBeanBuilder()
    {
    }

    public ConnectIssueTabPanelCapabilityBeanBuilder(ConnectIssueTabPanelCapabilityBean defaultBean)
    {
        super(defaultBean);
    }

    @Override
    public ConnectIssueTabPanelCapabilityBean build()
    {
        return new ConnectIssueTabPanelCapabilityBean(this);
    }
}
