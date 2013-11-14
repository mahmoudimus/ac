package com.atlassian.json.schema.model;

import java.util.List;

import com.atlassian.json.schema.SchemaTypes;

public class InterfaceSchema extends RootSchema
{
    List<ObjectSchema> anyOf;
    
    public InterfaceSchema()
    {
        setType(SchemaTypes.OBJECT);
    }

    public void setAnyOf(List<ObjectSchema> anyOf)
    {
        this.anyOf = anyOf;
    }

    public List<ObjectSchema> getAnyOf()
    {
        return anyOf;
    }
}
