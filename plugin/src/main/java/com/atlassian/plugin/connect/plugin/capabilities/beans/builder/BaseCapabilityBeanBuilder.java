package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.api.capabilities.beans.CapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.BaseCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.I18nProperty;

/**
 * @since version
 */
public abstract class BaseCapabilityBeanBuilder<T extends BaseCapabilityBeanBuilder, B extends BaseCapabilityBean>
{
    private String key;
    private I18nProperty name;
    private I18nProperty description;

    public BaseCapabilityBeanBuilder()
    {
    }

    public BaseCapabilityBeanBuilder(CapabilityBean defaultBean)
    {
        this.key = defaultBean.getKey();
        this.name = defaultBean.getName();
        this.description = defaultBean.getDescription();
    }

    public T withKey(String key)
    {
        this.key = key;
        return (T) this;
    }

    public T withName(I18nProperty name)
    {
        this.name = name;
        return (T) this;
    }

    public T withDescription(I18nProperty description)
    {
        this.description = description;
        return (T) this;
    }

    public B build()
    {
        return (B) new BaseCapabilityBean(this);
    }
}