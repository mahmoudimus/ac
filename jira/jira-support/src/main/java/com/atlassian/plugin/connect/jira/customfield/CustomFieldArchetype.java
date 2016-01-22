package com.atlassian.plugin.connect.jira.customfield;

/**
 * This enum lists all archetypes that a remote custom field type can configure.
 * It consists of a custom field type definition and a custom field searcher.
 */
public enum CustomFieldArchetype
{
    STRING(CustomFieldBaseType.TEXT, CustomFieldSearcherBase.EXACT_TEXT),
    TEXT(CustomFieldBaseType.TEXT, CustomFieldSearcherBase.LIKE_TEXT);

    private final CustomFieldBaseType type;
    private final CustomFieldSearcherBase searcherBase;

    CustomFieldArchetype(final CustomFieldBaseType type, final CustomFieldSearcherBase searcherBase)
    {
        this.type = type;
        this.searcherBase = searcherBase;
    }

    public CustomFieldBaseType getType()
    {
        return type;
    }

    public CustomFieldSearcherBase getSearcherBase()
    {
        return searcherBase;
    }
}
