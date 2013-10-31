package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectProjectAdminTabPanelCapabilityBean;

/**
 * Builder for a ConnectProjectAdminTabPanelCapabilityBean
 */
public class ConnectProjectAdminTabPanelCapabilityBeanBuilder
        extends ConnectTabPanelCapabilityBeanBuilder<ConnectProjectAdminTabPanelCapabilityBeanBuilder, ConnectProjectAdminTabPanelCapabilityBean>
{
    private String location;

    public ConnectProjectAdminTabPanelCapabilityBeanBuilder()
    {
    }

    public ConnectProjectAdminTabPanelCapabilityBeanBuilder(ConnectProjectAdminTabPanelCapabilityBean defaultBean)
    {
        super(defaultBean);
        location = defaultBean.getLocation();
    }

    public ConnectProjectAdminTabPanelCapabilityBeanBuilder withLocation(String location)
    {
        this.location = location;
        return this;
    }

    @Override
    public ConnectProjectAdminTabPanelCapabilityBean build()
    {
        return new ConnectProjectAdminTabPanelCapabilityBean(this);
    }

    public String getLocation()
    {
        return location;
    }

}
