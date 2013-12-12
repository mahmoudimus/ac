package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

public enum MacroBodyType
{
    /**
     * If this macro allows its body to contain rich content such as wiki markup
     */
    RICH_TEXT("rich-text"),

    /**
     * If this macro can only contain plain text
     */
    PLAIN_TEXT("plain-text"),

    /**
     * If this macro has no body
     */
    NONE("none");

    private final String value;

    MacroBodyType(String value)
    {
        this.value = value;
    }

    public String toString()
    {
        return value;
    }
}
