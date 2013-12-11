package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConfigurePageModuleBean;

public class ConfigurePageModuleBeanBuilder<T extends ConfigurePageModuleBeanBuilder, B extends ConfigurePageModuleBean>
        extends ConnectPageModuleBeanBuilder<T, B>
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

    public void setAsDefault()
    {
        isDefault = true;
    }

    @Override
    public B build()
    {
        return (B) new ConfigurePageModuleBean(this);
    }

}
