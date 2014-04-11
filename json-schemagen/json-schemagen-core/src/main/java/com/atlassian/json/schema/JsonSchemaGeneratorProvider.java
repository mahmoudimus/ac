package com.atlassian.json.schema;

import com.atlassian.json.schema.doclet.model.JsonSchemaDocs;
import com.atlassian.json.schema.scanner.model.InterfaceList;

public interface JsonSchemaGeneratorProvider
{

    JsonSchemaGenerator provide(EnumCase enumCase, InterfaceList interfaceList, JsonSchemaDocs schemaDocs, String ignoreFilter);
}
