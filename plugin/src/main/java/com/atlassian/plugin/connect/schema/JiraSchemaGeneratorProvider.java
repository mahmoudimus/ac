package com.atlassian.plugin.connect.schema;

import com.atlassian.json.schema.EnumCase;
import com.atlassian.json.schema.JsonSchemaGenerator;
import com.atlassian.json.schema.JsonSchemaGeneratorProvider;
import com.atlassian.json.schema.doclet.model.JsonSchemaDocs;
import com.atlassian.json.schema.scanner.model.InterfaceList;
import com.atlassian.plugin.connect.modules.util.ProductFilter;

public class JiraSchemaGeneratorProvider implements JsonSchemaGeneratorProvider
{
    @Override
    public JsonSchemaGenerator provide(EnumCase enumCase, InterfaceList interfaceList, JsonSchemaDocs schemaDocs, String ignoreFilter, String shortClassnameField)
    {
        return new ConnectSchemaGenerator(enumCase,interfaceList,schemaDocs, ignoreFilter, shortClassnameField, ProductFilter.JIRA);
    }
}
