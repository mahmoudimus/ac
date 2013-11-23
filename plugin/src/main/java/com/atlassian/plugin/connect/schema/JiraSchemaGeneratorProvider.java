package com.atlassian.plugin.connect.schema;

import com.atlassian.json.schema.JsonSchemaGenerator;
import com.atlassian.json.schema.JsonSchemaGeneratorProvider;
import com.atlassian.json.schema.doclet.model.JsonSchemaDocs;
import com.atlassian.json.schema.scanner.model.InterfaceList;
import com.atlassian.plugin.spring.scanner.ProductFilter;

public class JiraSchemaGeneratorProvider implements JsonSchemaGeneratorProvider
{
    @Override
    public JsonSchemaGenerator provide(boolean lowercaseEnums, InterfaceList interfaceList, JsonSchemaDocs schemaDocs)
    {
        return new ConnectSchemaGenerator(lowercaseEnums,interfaceList,schemaDocs, ProductFilter.JIRA);
    }
}
