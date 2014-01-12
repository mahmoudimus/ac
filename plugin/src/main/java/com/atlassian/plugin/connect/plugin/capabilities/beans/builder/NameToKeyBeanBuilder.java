package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.NameToKeyBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;

/**
 * @since 1.0
 */
public class NameToKeyBeanBuilder<T extends NameToKeyBeanBuilder, B extends NameToKeyBean> extends BaseModuleBeanBuilder<T,B>
{
    private String key;
    private I18nProperty name;

    public NameToKeyBeanBuilder()
    {
    }

    public NameToKeyBeanBuilder(NameToKeyBean defaultBean)
    {
        this.name = defaultBean.getName();
        this.key = defaultBean.getRawKey();
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

    @Override
    public B build()
    {
        return (B) new NameToKeyBean(this);
    }
}
