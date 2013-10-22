package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectVersionTabPanelCapabilityBean;

/**
 * Builder for a ConnectVersionTabPanelCapabilityBean
 */
public class ConnectVersionTabPanelCapabilityBeanBuilder
        extends AbstractConnectTabPanelCapabilityBeanBuilder<ConnectVersionTabPanelCapabilityBeanBuilder, ConnectVersionTabPanelCapabilityBean>
{

    public ConnectVersionTabPanelCapabilityBeanBuilder()
    {
    }

    public ConnectVersionTabPanelCapabilityBeanBuilder(ConnectVersionTabPanelCapabilityBean defaultBean)
    {
        super(defaultBean);
    }

    @Override
    public ConnectVersionTabPanelCapabilityBean build()
    {
        return new ConnectVersionTabPanelCapabilityBean(this);
    }
}
