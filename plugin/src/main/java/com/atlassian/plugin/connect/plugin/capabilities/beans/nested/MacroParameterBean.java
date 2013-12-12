package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import com.atlassian.plugin.connect.plugin.capabilities.beans.BaseModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.MacroParameterBeanBuilder;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class MacroParameterBean extends BaseModuleBean
{
    private String name;
    private MacroParameterType type;
    private Boolean required;
    private Boolean multiple;
    private String defaultValue;
    private List<String> values;
    private List<String> aliases;

    public MacroParameterBean(MacroParameterBeanBuilder builder)
    {
        super(builder);
        if (null == name)
        {
            name = "";
        }
        if (null == type)
        {
            type = MacroParameterType.STRING;
        }
        if (null == required)
        {
            required = false;
        }
        if (null == multiple)
        {
            multiple = false;
        }
        if (null == defaultValue)
        {
            defaultValue = "";
        }
        if (null == values)
        {
            values = ImmutableList.of();
        }
        if (null == aliases)
        {
            aliases = ImmutableList.of();
        }
    }

    public String getName()
    {
        return name;
    }

    public MacroParameterType getType()
    {
        return type;
    }

    public Boolean getRequired()
    {
        return required;
    }

    public Boolean getMultiple()
    {
        return multiple;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public boolean hasDefaultValue()
    {
        return !Strings.isNullOrEmpty(defaultValue);
    }

    public List<String> getValues()
    {
        return values;
    }

    public List<String> getAliases()
    {
        return aliases;
    }

    public static MacroParameterBeanBuilder newMacroParameterBean()
    {
        return new MacroParameterBeanBuilder();
    }

    public static MacroParameterBeanBuilder newMacroParameterBean(MacroParameterBean defaultBean)
    {
        return new MacroParameterBeanBuilder(defaultBean);
    }
}
