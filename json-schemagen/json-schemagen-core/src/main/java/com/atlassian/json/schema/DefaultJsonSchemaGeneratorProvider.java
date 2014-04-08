package com.atlassian.json.schema;

import com.atlassian.json.schema.doclet.model.JsonSchemaDocs;
import com.atlassian.json.schema.scanner.model.InterfaceList;

public class DefaultJsonSchemaGeneratorProvider implements JsonSchemaGeneratorProvider
{
    @Override
    public JsonSchemaGenerator provide(boolean lowercaseEnums, InterfaceList interfaceList, JsonSchemaDocs schemaDocs, String ignoreFilter)
    {
        return new DefaultJsonSchemaGenerator(lowercaseEnums, interfaceList, schemaDocs, ignoreFilter);
    }
}
