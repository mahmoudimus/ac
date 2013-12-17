package com.atlassian.json.schema.model;

import com.atlassian.json.schema.SchemaType;

public class MapTypeSchema extends BasicSchema
{
    private boolean additionalProperties;

    public MapTypeSchema()
    {
        this.additionalProperties = true;
        setType(SchemaType.OBJECT.name().toLowerCase());
    }
}
