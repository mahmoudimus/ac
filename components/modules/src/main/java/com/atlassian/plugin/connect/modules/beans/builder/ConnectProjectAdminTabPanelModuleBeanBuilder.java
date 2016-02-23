package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.ConnectProjectAdminTabPanelModuleBean;

/**
 * Builder for a ConnectProjectAdminTabPanelModuleBean
 */
public class ConnectProjectAdminTabPanelModuleBeanBuilder
        extends ConnectTabPanelModuleBeanBuilder<ConnectProjectAdminTabPanelModuleBeanBuilder, ConnectProjectAdminTabPanelModuleBean> {
    private String location;

    public ConnectProjectAdminTabPanelModuleBeanBuilder() {
    }

    public ConnectProjectAdminTabPanelModuleBeanBuilder(ConnectProjectAdminTabPanelModuleBean defaultBean) {
        super(defaultBean);
        location = defaultBean.getLocation();
    }

    public ConnectProjectAdminTabPanelModuleBeanBuilder withLocation(String location) {
        this.location = location;
        return this;
    }

    @Override
    public ConnectProjectAdminTabPanelModuleBean build() {
        return new ConnectProjectAdminTabPanelModuleBean(this);
    }

    public String getLocation() {
        return location;
    }

}
