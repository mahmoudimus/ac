package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.ControlBean;

public class ControlBeanBuilder extends NamedBeanBuilder<ControlBeanBuilder, ControlBean>
{
    private String key;
    private String type;

    public ControlBeanBuilder()
    {

    }

    public ControlBeanBuilder(ControlBean bean)
    {
        this.key = bean.getKey();
        this.type = bean.getType();
    }

    public ControlBeanBuilder withKey(String key)
    {
        this.key = key;
        return this;
    }

    public ControlBeanBuilder withType(String type)
    {
        this.type = type;
        return this;
    }

    @Override
    public ControlBean build()
    {
        return new ControlBean(this);
    }
}
