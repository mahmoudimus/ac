package com.atlassian.plugin.connect.modules.beans.nested;

/**
 * How this macro should be placed along side other page content
 *
 * @schemaTitle Macro Output Type
 * @since 1.0
 */
public enum MacroOutputType {
    /**
     * If the macro output should be displayed on a new line as a block
     */
    BLOCK("block"),

    /**
     * If the macro output should be displayed within the existing content
     */
    INLINE("inline");

    private final String value;

    MacroOutputType(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }

}
