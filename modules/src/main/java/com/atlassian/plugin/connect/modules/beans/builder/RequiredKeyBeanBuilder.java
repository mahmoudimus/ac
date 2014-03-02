package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.RequiredKeyBean;

/**
 * @since 1.0
 */
public class RequiredKeyBeanBuilder<T extends RequiredKeyBeanBuilder, B extends RequiredKeyBean> extends NamedBeanBuilder<T, B>
{
    private String key;

    public RequiredKeyBeanBuilder()
    {
    }

    public RequiredKeyBeanBuilder(RequiredKeyBean defaultBean)
    {
        super(defaultBean);
        
        this.key = defaultBean.getRawKey();
    }

    public T withKey(String key)
    {
        this.key = key;
        return (T) this;
    }

    @Override
    public B build()
    {
        return (B) new RequiredKeyBean(this);
    }
}
