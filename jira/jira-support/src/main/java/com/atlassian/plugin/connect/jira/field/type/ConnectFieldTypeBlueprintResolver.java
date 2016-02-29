package com.atlassian.plugin.connect.jira.field.type;

import com.atlassian.plugin.connect.modules.beans.ConnectFieldType;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableMap;

import static com.atlassian.plugin.connect.modules.beans.ConnectFieldType.STRING;
import static com.atlassian.plugin.connect.modules.beans.ConnectFieldType.TEXT;

/**
 * Maps {@link ConnectFieldType}s to their respective {@link CustomFieldTypeBlueprint}s.
 */
@JiraComponent
public class ConnectFieldTypeBlueprintResolver {
    private final ImmutableMap<ConnectFieldType, CustomFieldTypeBlueprint> map;

    public ConnectFieldTypeBlueprintResolver() {
        map = ImmutableMap.<ConnectFieldType, CustomFieldTypeBlueprint>builder()
                .put(STRING, definition(CustomFieldTypeDefinition.TEXT, SearcherDefinition.EXACT_TEXT))
                .put(TEXT, definition(CustomFieldTypeDefinition.TEXT, SearcherDefinition.LIKE_TEXT))
                .build();
    }

    public CustomFieldTypeBlueprint getBlueprint(ConnectFieldType fieldType) {
        return map.get(fieldType);
    }

    private CustomFieldTypeBlueprint definition(final CustomFieldTypeDefinition typeDefinition, final SearcherDefinition searcherDefinition) {
        return new CustomFieldTypeBlueprint(typeDefinition, searcherDefinition);
    }
}
