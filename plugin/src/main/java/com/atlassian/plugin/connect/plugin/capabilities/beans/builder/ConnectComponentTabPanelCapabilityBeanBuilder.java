package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectComponentTabPanelCapabilityBean;

/**
 * Builder for a ConnectComponentTabPanelCapabilityBean
 */
public class ConnectComponentTabPanelCapabilityBeanBuilder
        extends AbstractConnectTabPanelCapabilityBeanBuilder<ConnectComponentTabPanelCapabilityBeanBuilder, ConnectComponentTabPanelCapabilityBean>
{

    public ConnectComponentTabPanelCapabilityBeanBuilder()
    {
    }

    public ConnectComponentTabPanelCapabilityBeanBuilder(ConnectComponentTabPanelCapabilityBean defaultBean)
    {
        super(defaultBean);
    }

    @Override
    public ConnectComponentTabPanelCapabilityBean build()
    {
        return new ConnectComponentTabPanelCapabilityBean(this);
    }
}
