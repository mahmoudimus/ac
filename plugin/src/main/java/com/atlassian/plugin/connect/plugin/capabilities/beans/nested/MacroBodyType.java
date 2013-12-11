package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

public enum MacroBodyType
{
    RICH_TEXT("rich-text"),
    PLAIN_TEXT("plain-text"),
    NONE("none");

    private final String bodyType;

    MacroBodyType(String bodyType)
    {
        this.bodyType = bodyType;
    }

    public String toString()
    {
        return bodyType;
    }
}
