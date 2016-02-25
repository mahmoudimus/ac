package com.atlassian.plugin.connect.modules.beans.nested.contenttype;

/**
 * Body type for the Extensible Content Type
 *
 * @schemaTitle Extensible Content Type Body Type
 *
 * @since 1.1.77
 */
public enum BodyType
{
    /**
     * If this Extensible Content Type will have Confluence Storage Format as the content body.
     *
     * Please consult <a href="https://confluence.atlassian.com/display/DOC/Confluence+Storage+Format">Confluence Storage Format</a>
     * for additional information about how to construct valid storage format XML.
     */
    STORAGE("storage"),

    /**
     * If this Extensible Content Type will have Confluence wiki markup as the content body.
     *
     * Please consult <a href="https://confluence.atlassian.com/display/DOC/Confluence+Wiki+Markup">Confluence Wiki Markup</a>
     * for additional information about Confluence Wiki Markup syntax.
     */
    WIKI("wiki"),

    /**
     * If this Extensible Content Type will have any format other than <code>storage</code> or <code>wiki</code> as the content body. For example: images, achieve, etc.
     */
    RAW("raw");

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
