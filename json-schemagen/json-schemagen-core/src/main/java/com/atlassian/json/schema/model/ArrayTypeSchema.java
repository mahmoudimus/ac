package com.atlassian.json.schema.model;

import com.atlassian.json.schema.SchemaTypes;

public class ArrayTypeSchema extends BasicSchema
{
    private JsonSchema items;
    
    public ArrayTypeSchema()
    {
        setType(SchemaTypes.ARRAY);
    }

    public void setItems(JsonSchema items)
    {
        this.items = items;
    }
}
