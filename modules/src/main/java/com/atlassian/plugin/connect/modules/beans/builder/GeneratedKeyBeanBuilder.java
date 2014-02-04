package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.GeneratedKeyBean;
import com.atlassian.plugin.connect.modules.beans.NamedBean;

/**
 * @since 1.0
 */
public class GeneratedKeyBeanBuilder<T extends GeneratedKeyBeanBuilder, B extends NamedBean> extends NamedBeanBuilder<T, B>
{
    private String key;

    public GeneratedKeyBeanBuilder()
    {
    }

    public GeneratedKeyBeanBuilder(GeneratedKeyBean defaultBean)
    {
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
        return (B) new GeneratedKeyBean(this);
    }
}
