package com.atlassian.json.schema;

import java.lang.reflect.Field;

import com.atlassian.json.schema.model.ObjectSchema;
import com.atlassian.json.schema.model.RootSchema;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonSchemaGenerator
{
    private final SchemaFactory schemaFactory;

    public JsonSchemaGenerator()
    {
        this(new DefaultSchemaFactory());
    }

    public JsonSchemaGenerator(SchemaFactory factory)
    {
        this.schemaFactory = factory;
    }

    public String generateSchema(Class<?> rootClass)
    {
        RootSchema rootSchema = (RootSchema) schemaFactory.generateSchema(rootClass);
        rootSchema.addDollarSchema();

        GsonBuilder gb = new GsonBuilder();
        gb.setPrettyPrinting();
        
        Gson gson = gb.create();
        
        return gson.toJson(rootSchema);
    }
        
}
