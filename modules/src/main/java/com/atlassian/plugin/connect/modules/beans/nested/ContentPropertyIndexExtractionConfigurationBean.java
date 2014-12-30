package com.atlassian.plugin.connect.modules.beans.nested;

/**
 * Representation of a extraction recipe for a single JSON value.
 *
 * @since 1.1.20
 */
public class ContentPropertyIndexExtractionConfigurationBean
{
    private final String path;
    private final String type;

    public ContentPropertyIndexExtractionConfigurationBean(String path, String type)
    {
        this.path = path;
        this.type = type;
    }

    public String getPath()
    {
        return path;
    }

    public String getType()
    {
        return type;
    }
}
