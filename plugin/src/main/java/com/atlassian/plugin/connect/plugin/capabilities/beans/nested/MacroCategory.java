package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

public enum MacroCategory
{
    FORMATTING("formatting"),
    CONFLUENCE_CONTENT("confluence-content"),
    MEDIA("media"),
    VISUALS("visuals"),
    NAVIGATION("navigation"),
    EXTERNAL_CONTENT("external-content"),
    COMMUNICATION("communication"),
    REPORTING("reporting"),
    ADMIN("admin"),
    DEVELOPMENT("development"),
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
