package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

public enum MacroOutputType
{
    /**
     * If the macro output should be displayed on a new line as a block
     */
    BLOCK("block"),

    /**
     * If the macro output should be displayed within the existing content
     */
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
