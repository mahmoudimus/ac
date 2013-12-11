package com.atlassian.json.schema.model;

import com.atlassian.json.schema.SchemaType;

public class InterfaceSchema extends BasicSchema
{
    public InterfaceSchema()
    {
        setType(SchemaType.OBJECT.name().toLowerCase());
    }
}
