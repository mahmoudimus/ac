package com.atlassian.plugin.connect.stash;

import com.atlassian.json.schema.EnumCase;
import com.atlassian.json.schema.JsonSchemaGenerator;
import com.atlassian.json.schema.JsonSchemaGeneratorProvider;
import com.atlassian.json.schema.doclet.model.JsonSchemaDocs;
import com.atlassian.json.schema.scanner.model.InterfaceList;
import com.atlassian.plugin.connect.api.capabilities.descriptor.ConnectSchemaGenerator;
import com.atlassian.plugin.connect.modules.util.ProductFilter;

public class StashSchemaGeneratorProvider implements JsonSchemaGeneratorProvider
{
    @Override
    public JsonSchemaGenerator provide(EnumCase enumCase, InterfaceList interfaceList, JsonSchemaDocs schemaDocs, String ignoreFilter)
    {
        return new ConnectSchemaGenerator(enumCase, interfaceList, schemaDocs, ignoreFilter, ProductFilter.STASH);
    }
}
