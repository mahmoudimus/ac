package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

public enum MacroOutputType
{
    BLOCK("block"),
    INLINE("inline");

    private final String value;

    private MacroOutputType(String value)
    {
        this.value = value;
    }

    public String toString()
    {
        return value;
    }

}
