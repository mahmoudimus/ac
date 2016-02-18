package com.atlassian.plugin.connect.modules.beans.nested.contenttype;

/**
 * Body type for the Extensible Content Type
 *
 * @schemaTitle Extensible Content Type Body Type
 * @since 1.1
 */
public enum BodyType
{
    STORAGE("storage"),

    JSON("json"),

    BINARY("binary"),

    WIKI("wiki");

    private final String value;

    BodyType(String value)
    {
        this.value = value;
    }

    public String toString()
    {
        return value;
    }
}
