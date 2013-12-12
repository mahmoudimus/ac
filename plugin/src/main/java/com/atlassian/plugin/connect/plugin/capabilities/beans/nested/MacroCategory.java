package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

public enum MacroCategory
{
    /**
     * Formatting
     */
    FORMATTING("formatting"),

    /**
     * Confluence Content
     */
    CONFLUENCE_CONTENT("confluence-content"),

    /**
     * Media
     */
    MEDIA("media"),

    /**
     * Visuals & Images
     */
    VISUALS("visuals"),

    /**
     * Navigation
     */
    NAVIGATION("navigation"),

    /**
     * External Content
     */
    EXTERNAL_CONTENT("external-content"),

    /**
     * Communication
     */
    COMMUNICATION("communication"),

    /**
     * Reporting
     */
    REPORTING("reporting"),

    /**
     * Administration
     */
    ADMIN("admin"),

    /**
     * Development
     */
    DEVELOPMENT("development"),

    /**
     * Hidden
     */
    HIDDEN("hidden-macros");

    private final String category;

    private MacroCategory(String category)
    {
        this.category = category;
    }

    public String toString()
    {
        return category;
    }
}
