package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConfigurePageModuleBean;

public class ConfigurePageModuleBeanBuilder
        extends ConnectPageModuleBeanBuilder<ConfigurePageModuleBeanBuilder, ConfigurePageModuleBean>
{
    private Boolean isDefault;

    public ConfigurePageModuleBeanBuilder()
    {
    }

    public ConfigurePageModuleBeanBuilder(ConfigurePageModuleBean defaultBean)
    {
        super(defaultBean);
        isDefault = defaultBean.isDefault();
    }

    public ConfigurePageModuleBeanBuilder setAsDefault()
    {
        isDefault = true;
        return this;
    }

    @Override
    public ConfigurePageModuleBean build()
    {
        return new ConfigurePageModuleBean(this);
    }

}
