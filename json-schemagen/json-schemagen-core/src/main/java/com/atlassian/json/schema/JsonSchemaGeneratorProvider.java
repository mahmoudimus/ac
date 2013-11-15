package com.atlassian.json.schema;

import com.atlassian.json.schema.doclet.model.JsonSchemaDocs;
import com.atlassian.json.schema.scanner.model.InterfaceList;

public class JsonSchemaGeneratorProvider
{
    public static JsonSchemaGenerator provide(boolean lowercaseEnums, InterfaceList interfaceList, JsonSchemaDocs schemaDocs)
    {
        return new DefaultJsonSchemaGenerator(lowercaseEnums, interfaceList, schemaDocs);
    }
}
