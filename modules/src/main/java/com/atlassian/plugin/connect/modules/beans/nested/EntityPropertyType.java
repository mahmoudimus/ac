package com.atlassian.plugin.connect.modules.beans.nested;

public enum EntityPropertyType
{
    issue("IssueProperty");

    private final String value;

    private EntityPropertyType(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }
}
