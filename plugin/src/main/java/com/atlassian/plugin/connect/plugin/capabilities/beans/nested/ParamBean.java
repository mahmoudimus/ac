package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

/**
 * @since 1.0
 */
public class ParamBean
{
    private String name;
    private String value;

    public ParamBean(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public String getValue()
    {
        return value;
    }

    public static ParamBean newParamBean(String name, String value)
    {
        return new ParamBean(name,value);
    }
}
