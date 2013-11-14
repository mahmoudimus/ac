package com.atlassian.json.schema;

import com.atlassian.json.schema.testobjects.FoodObject;

import org.junit.Test;

public class TestSchemaGenerator
{
    @Test
    public void testName() throws Exception
    {
        JsonSchemaGenerator generator = new JsonSchemaGenerator();
        String json = generator.generateSchema(FoodObject.class);

        System.out.println(json);
    }
}
