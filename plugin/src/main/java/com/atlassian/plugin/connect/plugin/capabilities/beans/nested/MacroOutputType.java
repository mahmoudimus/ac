package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

public enum MacroOutputType
{
    BLOCK("block"),
    INLINE("inline");

    private final String outputType;

    private MacroOutputType(String outputType)
    {
        this.outputType = outputType;
    }

    public String toString()
    {
        return outputType;
    }
}
