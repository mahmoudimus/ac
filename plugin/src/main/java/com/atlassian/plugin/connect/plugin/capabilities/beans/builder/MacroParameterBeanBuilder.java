package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroParameterBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroParameterType;

public class MacroParameterBeanBuilder extends BaseModuleBeanBuilder<MacroParameterBeanBuilder, MacroParameterBean>
{
    private String name;
    private MacroParameterType type;
    private Boolean required;
    private Boolean multiple;
    private String defaultValue;

    public MacroParameterBeanBuilder()
    {
    }

    public MacroParameterBeanBuilder(MacroParameterBean defaultBean)
    {
        this.name = defaultBean.getName();
        this.type = defaultBean.getType();
        this.required = defaultBean.getRequired();
        this.multiple = defaultBean.getMultiple();
        this.defaultValue = defaultBean.getDefaultValue();
    }

    public MacroParameterBeanBuilder withName(String name)
    {
        this.name = name;
        return this;
    }

    public MacroParameterBeanBuilder withType(MacroParameterType type)
    {
        this.type = type;
        return this;
    }

    public MacroParameterBeanBuilder withRequired(Boolean required)
    {
        this.required = required;
        return this;
    }

    public MacroParameterBeanBuilder withMultiple(Boolean multiple)
    {
        this.multiple = multiple;
        return this;
    }

    public MacroParameterBeanBuilder withDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public MacroParameterBean build()
    {
        return new MacroParameterBean(this);
    }
}

