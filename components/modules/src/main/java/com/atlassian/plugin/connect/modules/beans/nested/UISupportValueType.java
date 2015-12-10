package com.atlassian.plugin.connect.modules.beans.nested;

public enum UISupportValueType
{

    SPACE("space"),
    LABEL("label"),
    USER("user"),
    CONTENT_ID("contentId"),
    CONTENT_TYPE("contentType"),
    DATE("date"),
    STRING("string"),
    NUMBER("number");

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
