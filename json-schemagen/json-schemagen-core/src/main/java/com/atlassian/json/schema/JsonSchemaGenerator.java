package com.atlassian.json.schema;

import com.atlassian.json.schema.model.JsonSchema;

public interface JsonSchemaGenerator
{
    JsonSchema generateSchema(Class<?> rootClass);
}
