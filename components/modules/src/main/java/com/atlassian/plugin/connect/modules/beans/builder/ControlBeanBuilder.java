package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.ControlBean;

public class ControlBeanBuilder extends NamedBeanBuilder<ControlBeanBuilder, ControlBean>
{
    private String key;
    private String type;

    public ControlBeanBuilder()
    {

    }

    //TODO: Why are we calling this defaultBean?
    public ControlBeanBuilder(ControlBean defaultBean)
    {
        this.key = defaultBean.getKey();
        this.type = defaultBean.getType();
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
