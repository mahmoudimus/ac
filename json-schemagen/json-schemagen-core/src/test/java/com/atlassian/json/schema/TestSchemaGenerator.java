package com.atlassian.json.schema;

import com.atlassian.json.schema.doclet.model.JsonSchemaDocs;
import com.atlassian.json.schema.scanner.model.InterfaceListBuilder;
import com.atlassian.json.schema.testobjects.ComplexSmell;
import com.atlassian.json.schema.testobjects.FoodObject;
import com.atlassian.json.schema.testobjects.SimpleSmell;

import org.junit.Test;

public class TestSchemaGenerator
{
    @Test
    public void testName() throws Exception
    {
        JsonSchemaGenerator generator = JsonSchemaGeneratorProvider.provide(
                true
                , new InterfaceListBuilder()
                .withImplementation(SimpleSmell.class)
                .withImplementation(ComplexSmell.class)
                .build()
                , new JsonSchemaDocs()
        );

        String json = generator.generateSchema(FoodObject.class);

        System.out.println(json);
    }
}
