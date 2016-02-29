package com.atlassian.plugin.connect.jira.field.type;

/**
 * A class containing information needed to create a Connect field of a certain type.
 */
public final class CustomFieldTypeBlueprint {
    private final CustomFieldTypeDefinition typeDefinition;
    private final SearcherDefinition searcherDefinition;

    CustomFieldTypeBlueprint(final CustomFieldTypeDefinition typeDefinition, final SearcherDefinition searcherDefinition) {
        this.typeDefinition = typeDefinition;
        this.searcherDefinition = searcherDefinition;
    }

    public CustomFieldTypeDefinition getCustomFieldTypeDefinition() {
        return typeDefinition;
    }

    public SearcherDefinition getSearcherDefinition() {
        return searcherDefinition;
    }
}
