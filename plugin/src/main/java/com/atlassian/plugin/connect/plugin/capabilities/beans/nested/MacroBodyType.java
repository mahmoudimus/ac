package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

public enum MacroBodyType
{
    RICH_TEXT("rich-text"),
    PLAIN_TEXT("plain-text"),
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
