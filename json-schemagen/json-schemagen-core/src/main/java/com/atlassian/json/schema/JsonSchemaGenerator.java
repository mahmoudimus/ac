package com.atlassian.json.schema;

public interface JsonSchemaGenerator
{
    String generateSchema(Class<?> rootClass);
}
