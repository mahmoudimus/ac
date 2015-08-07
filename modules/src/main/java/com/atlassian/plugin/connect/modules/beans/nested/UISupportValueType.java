package com.atlassian.plugin.connect.modules.beans.nested;

public enum UISupportValueType
{
    STRING("string"),

    TEXT("text"),

    NUMBER("number"),

    DATE("date");

    private final String value;

    UISupportValueType(String value)
    {
        this.value = value;
    }

    public String toString()
    {
        return value;
    }

}
