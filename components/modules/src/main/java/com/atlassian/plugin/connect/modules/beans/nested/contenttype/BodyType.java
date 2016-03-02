package com.atlassian.plugin.connect.modules.beans.nested.contenttype;

/**
 * Body type for the Extensible Content Type
 *
 * @schemaTitle Extensible Content Type Body Type
 * @since 1.1.77
 */
public enum BodyType {
    /**
     * If this Extensible Content Type will have Confluence Storage Format as the content body.
     * <p>
     * Please consult <a href="https://confluence.atlassian.com/display/DOC/Confluence+Storage+Format">Confluence Storage Format</a>
     * for additional information about how to construct valid storage format XML.
     */
    STORAGE("storage");

    private final String value;

    BodyType(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }
}
