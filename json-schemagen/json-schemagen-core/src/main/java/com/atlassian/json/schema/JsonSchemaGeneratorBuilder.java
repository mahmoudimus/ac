package com.atlassian.json.schema;

import com.atlassian.json.schema.scanner.model.InterfaceList;

public class JsonSchemaGeneratorBuilder
{
    private boolean lowercaseEnums;
    private InterfaceList interfaceList;

    public JsonSchemaGeneratorBuilder()
    {
        this.lowercaseEnums = false;
        this.interfaceList = new InterfaceList();
    }

    public JsonSchemaGeneratorBuilder withLowercaseEnums()
    {
        this.lowercaseEnums = true;
        return this;
    }

    public JsonSchemaGeneratorBuilder withInterfaceList(InterfaceList lst)
    {
        this.interfaceList = lst;
        return this;
    }

    public JsonSchemaGenerator build()
    {
        return new JsonSchemaGenerator(lowercaseEnums, interfaceList);
    }
}
