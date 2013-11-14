package com.atlassian.json.schema;

import com.atlassian.json.schema.model.JsonSchema;

public interface SchemaFactory
{
    JsonSchema generateSchema(Class<?> clazz);
}
