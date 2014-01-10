package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

/**
 * @since 1.0
 */
public enum EntityPropertyType
{
    /**
     * Properties associated with issues.
     */
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
