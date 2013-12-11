package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

public enum MacroParameterType
{
    /**
     * A raw string, or some other type not handled by confluence.
     */
    STRING("string"),
    /**
     * A boolean value.
     */
    BOOLEAN("boolean"),
    /**
     * A user. Stored as a username string. Converted to a user object when returned.
     */
    USERNAME("username"),
    /**
     * A string, chosen from a limited set of values.
     */
    ENUM("enum"),
    /**
     * An integer.
     */
    INT("int"),
    /**
     * A space key.
     */
    SPACE_KEY("spacekey"),
    /**
     * A date range, specified by one or two time offsets (e.g. 1d or 1d,2d).
     */
    RELATIVE_DATE("relativedate"),
    /**
     * A number, including a percent sign. Stored as a string.
     */
    PERCENTAGE("percentage"),
    /**
     * A content entity title (optionally qualified by space key and/or publication date).
     */
    CONFLUENCE_CONTENT("confluence-content"),
    /**
     * A URL.
     */
    URL("url"),
    /**
     * A colour in CSS format. Stored as a string.
     */
    COLOR("color"),
    /**
     * An attachment name (optionally qualified by container content title, space key and/or publication date).
     */
    ATTACHMENT("attachment"),
    /**
     * A label.
     */
    LABEL("label"),
    /**
     * A blog publication date (optionally qualified by space key).
     */
    DATE("date"),
    /**
     * A group.
     */
    GROUP("group");

    private final String type;

    private MacroParameterType(String type)
    {
        this.type = type;
    }

    public String toString()
    {
        return type;
    }
}
