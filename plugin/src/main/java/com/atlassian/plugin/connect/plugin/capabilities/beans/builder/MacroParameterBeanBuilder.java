package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroParameterBean;
import com.google.common.collect.Lists;

import java.util.List;

public class MacroParameterBeanBuilder extends BaseModuleBeanBuilder<MacroParameterBeanBuilder, MacroParameterBean>
{
    private String identifier;
    private I18nProperty name;
    private I18nProperty description;
    private String type;
    private Boolean required;
    private Boolean multiple;
    private String defaultValue;
    private List<String> values;
    private List<String> aliases;

    public MacroParameterBeanBuilder()
    {
    }

    public MacroParameterBeanBuilder(MacroParameterBean defaultBean)
    {
        this.identifier = defaultBean.getIdentifier();
        this.name = defaultBean.getName();
        this.description = defaultBean.getDescription();
        this.type = defaultBean.getType();
        this.required = defaultBean.isRequired();
        this.multiple = defaultBean.isMultiple();
        this.defaultValue = defaultBean.getDefaultValue();
        this.values = defaultBean.getValues();
        this.aliases = defaultBean.getAliases();
    }

    public MacroParameterBeanBuilder withIdentifier(String identifier)
    {
        this.identifier = identifier;
        return this;
    }

    public MacroParameterBeanBuilder withName(I18nProperty name)
    {
        this.name = name;
        return this;
    }

    public MacroParameterBeanBuilder withDescription(I18nProperty name)
    {
        this.description = name;
        return this;
    }

    public MacroParameterBeanBuilder withType(String type)
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

    public MacroParameterBeanBuilder withValues(String... values)
    {
        this.values = Lists.newArrayList(values);
        return this;
    }

    public MacroParameterBeanBuilder withAliases(String... aliases)
    {
        this.aliases = Lists.newArrayList(aliases);
        return this;
    }

    @Override
    public MacroParameterBean build()
    {
        return new MacroParameterBean(this);
    }
}

