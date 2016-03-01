package com.atlassian.plugin.connect.jira.field;

import com.atlassian.plugin.connect.jira.field.type.ConnectFieldTypeBlueprintResolver;
import com.atlassian.plugin.connect.jira.field.type.CustomFieldTypeBlueprint;
import com.atlassian.plugin.connect.modules.beans.ConnectFieldType;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class CustomFieldTypeBlueprintResolverTest {
    @Test
    public void testFieldMapperMapsAllTypes() throws Exception {
        ConnectFieldTypeBlueprintResolver connectFieldTypeBlueprintResolver = new ConnectFieldTypeBlueprintResolver();

        for (ConnectFieldType connectFieldType : ConnectFieldType.values()) {
            CustomFieldTypeBlueprint mapping = connectFieldTypeBlueprintResolver.getBlueprint(connectFieldType);
            assertNotNull("Mapping for " + connectFieldType.toString() + " was missing", mapping);
        }
    }
}