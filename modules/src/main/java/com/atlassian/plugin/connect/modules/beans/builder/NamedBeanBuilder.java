package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.NamedBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

/**
 * @since 1.0
 */
public class NamedBeanBuilder<T extends NamedBeanBuilder, B extends NamedBean> extends BaseModuleBeanBuilder<T, B>
{
    private I18nProperty name;

    public NamedBeanBuilder()
    {
    }

    public NamedBeanBuilder(NamedBean defaultBean)
    {
        this.name = defaultBean.getName();
    }

    public T withName(I18nProperty name)
    {
        this.name = name;
        return (T) this;
    }

    @Override
    public B build()
    {
        return (B) new NamedBean(this);
    }
}
