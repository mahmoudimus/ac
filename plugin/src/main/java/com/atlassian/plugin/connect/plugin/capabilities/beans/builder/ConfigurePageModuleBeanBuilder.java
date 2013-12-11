package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import java.util.Map;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConditionalBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConfigurePageModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;

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

    public T setAsDefault()
    {
        isDefault = true;
        return (T) this;
    }

    public T withUrl(String url)
    {
        return (T) super.withUrl(url);
    }

    public T withWeight(int weight)
    {
        return (T) super.withWeight(weight);
    }

    @Override
    public T withConditions(ConditionalBean... beans)
    {
        super.withConditions(beans);
        return (T) this;
    }

    @Override
    public T withParams(Map<String, String> params)
    {
        super.withParams(params);
        return (T) this;
    }

    @Override
    public T withParam(String key, String value)
    {
        super.withParam(key, value);
        return (T) this;
    }

    @Override
    public T withKey(String key)
    {
        super.withKey(key);
        return (T) this;
    }

    @Override
    public T withName(I18nProperty name)
    {
        super.withName(name);
        return (T) this;
    }

    public T withLocation(String location)
    {
        return (T) super.withLocation(location);
    }

    public T withIcon(IconBean icon)
    {
        return (T) super.withIcon(icon);
    }

    @Override
    public B build()
    {
        return (B) new ConfigurePageModuleBean(this);
    }

}
