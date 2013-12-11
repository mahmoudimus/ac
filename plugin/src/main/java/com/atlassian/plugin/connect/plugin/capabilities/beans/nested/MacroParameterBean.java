package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import com.atlassian.plugin.connect.plugin.capabilities.beans.BaseModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.MacroParameterBeanBuilder;
import com.google.common.base.Strings;

public class MacroParameterBean extends BaseModuleBean
{
    private String name;
    private MacroParameterType type;
    private Boolean required;
    private Boolean multiple;
    private String defaultValue;

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

    public static MacroParameterBeanBuilder newMacroParameterBean()
    {
        return new MacroParameterBeanBuilder();
    }

    public static MacroParameterBeanBuilder newMacroParameterBean(MacroParameterBean defaultBean)
    {
        return new MacroParameterBeanBuilder(defaultBean);
    }
}
