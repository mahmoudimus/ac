package com.atlassian.json.schema;

import com.atlassian.json.schema.doclet.model.JsonSchemaDocs;
import com.atlassian.json.schema.scanner.model.InterfaceList;

public interface JsonSchemaGeneratorProvider
{

    JsonSchemaGenerator provide(boolean lowercaseEnums, InterfaceList interfaceList, JsonSchemaDocs schemaDocs, String ignoreFilter);
}
