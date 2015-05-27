package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.MacroParameterBean;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

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
    private Map<String, String> options;

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
        this.options = defaultBean.getOptions();
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

    public MacroParameterBeanBuilder withOption(String key, String value)
    {
        if (this.options == null)
            this.options = newHashMap();
        options.put(key, value);
        return this;
    }

    public MacroParameterBeanBuilder withOptions(Map<String, String> options)
    {
        this.options = newHashMap(options);
        return this;
    }

    @Override
    public MacroParameterBean build()
    {
        return new MacroParameterBean(this);
    }
}

