package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectProjectTabPanelCapabilityBean;

/**
 * Builder for a ConnectProjectTabPanelCapabilityBean
 */
public class ConnectProjectTabPanelCapabilityBeanBuilder
        extends AbstractConnectTabPanelCapabilityBeanBuilder<ConnectProjectTabPanelCapabilityBeanBuilder, ConnectProjectTabPanelCapabilityBean>
{

    public ConnectProjectTabPanelCapabilityBeanBuilder()
    {
    }

    public ConnectProjectTabPanelCapabilityBeanBuilder(ConnectProjectTabPanelCapabilityBean defaultBean)
    {
        super(defaultBean);
    }

    @Override
    public ConnectProjectTabPanelCapabilityBean build()
    {
        return new ConnectProjectTabPanelCapabilityBean(this);
    }
}
