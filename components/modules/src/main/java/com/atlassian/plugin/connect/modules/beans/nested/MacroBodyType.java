package com.atlassian.plugin.connect.modules.beans.nested;

/**
 * The type of body content, if any, for this macro
 *
 * @schemaTitle Macro Body Type
 * @since 1.0
 */
public enum MacroBodyType {
    /**
     * If this macro allows its body to contain rich content such as wiki markup
     */
    RICH_TEXT("rich-text"),

    /**
     * If this macro can only contain plain text
     */
    PLAIN_TEXT("plain-text"),

    /**
     * If this macro has no body
     */
    NONE("none");

    private final String value;

    MacroBodyType(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }
}
