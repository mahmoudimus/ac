package com.atlassian.json.schema;

import com.atlassian.json.schema.doclet.model.JsonSchemaDocs;
import com.atlassian.json.schema.doclet.model.SchemaClassDoc;
import com.atlassian.json.schema.doclet.model.SchemaFieldDoc;
import com.atlassian.json.schema.scanner.model.InterfaceListBuilder;
import com.atlassian.json.schema.testobjects.*;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class TestSchemaGenerator
{
    @Test
    public void testName() throws Exception
    {
        JsonSchemaGenerator generator = new DefaultJsonSchemaGeneratorProvider().provide(
                EnumCase.LOWER
                , new InterfaceListBuilder()
                .withImplementation(SimpleSmell.class)
                .withImplementation(ComplexSmell.class)
                .build()
                , getDocs()
                ,""
        );

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .setFieldNamingStrategy(new SchemaFieldNamingStrategy())
                .create();
        
        String json = gson.toJson(generator.generateSchema(FoodObject.class));

        System.out.println(json);
    }

    @Test
    public void testCommonAttrs() throws Exception
    {
        JsonSchemaGenerator generator = new DefaultJsonSchemaGeneratorProvider().provide(
                EnumCase.LOWER
                , new InterfaceListBuilder().build()
                , new JsonSchemaDocs()
                ,""
        );

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .setFieldNamingStrategy(new SchemaFieldNamingStrategy())
                .create();

        String json = gson.toJson(generator.generateSchema(CommonAttrs.class));

        System.out.println(json);
    }

    @Test
    public void testInheritance() throws Exception
    {
        JsonSchemaGenerator generator = new DefaultJsonSchemaGeneratorProvider().provide(
                EnumCase.LOWER
                , new InterfaceListBuilder().build()
                , new JsonSchemaDocs()
                ,""
        );

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .setFieldNamingStrategy(new SchemaFieldNamingStrategy())
                .create();

        String json = gson.toJson(generator.generateSchema(ChildObject.class));

        System.out.println(json);
        
        assertTrue(json.contains("parentField"));
    }
    
    @Test
    public void testStringAttrs() throws Exception
    {
        JsonSchemaGenerator generator = new DefaultJsonSchemaGeneratorProvider().provide(
                EnumCase.LOWER
                , new InterfaceListBuilder().build()
                , new JsonSchemaDocs()
                ,""
        );

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .setFieldNamingStrategy(new SchemaFieldNamingStrategy())
                .create();

        String json = gson.toJson(generator.generateSchema(StringAttrs.class));

        System.out.println(json);
    }

    @Test
    public void testNumericAttrs() throws Exception
    {
        JsonSchemaGenerator generator = new DefaultJsonSchemaGeneratorProvider().provide(
                EnumCase.LOWER
                , new InterfaceListBuilder().build()
                , new JsonSchemaDocs()
                ,""
        );

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .setFieldNamingStrategy(new SchemaFieldNamingStrategy())
                .create();

        String json = gson.toJson(generator.generateSchema(NumericAttrs.class));

        System.out.println(json);
    }

    @Test
    public void testArrayAttrs() throws Exception
    {
        JsonSchemaGenerator generator = new DefaultJsonSchemaGeneratorProvider().provide(
                EnumCase.LOWER
                , new InterfaceListBuilder().build()
                , new JsonSchemaDocs()
                ,""
        );

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .setFieldNamingStrategy(new SchemaFieldNamingStrategy())
                .create();

        String json = gson.toJson(generator.generateSchema(ArrayAttrs.class));

        System.out.println(json);
    }

    @Test
    public void testObjectAttrs() throws Exception
    {
        JsonSchemaGenerator generator = new DefaultJsonSchemaGeneratorProvider().provide(
                EnumCase.LOWER
                , new InterfaceListBuilder().build()
                , new JsonSchemaDocs()
                ,""
        );

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .setFieldNamingStrategy(new SchemaFieldNamingStrategy())
                .create();

        String json = gson.toJson(generator.generateSchema(ObjectAttrs.class));

        System.out.println(json);
    }
    
    
    public JsonSchemaDocs getDocs()
    {
        //TODO : make a builder for this
        List<SchemaClassDoc> classDocs = new ArrayList<SchemaClassDoc>();
        SchemaClassDoc classDoc = new SchemaClassDoc();
        classDoc.setClassName(FoodObject.class.getName());
        
        List<SchemaFieldDoc> fieldDocs = new ArrayList<SchemaFieldDoc>();
        SchemaFieldDoc fieldDoc = new SchemaFieldDoc();
        fieldDoc.setFieldName("color");
        fieldDoc.setFieldTitle("The Color");
        fieldDoc.setFieldDocs("The color of a food");
        fieldDocs.add(fieldDoc);
        
        classDoc.setClassTitle("Mmmm... Food");
        classDoc.setClassDoc("Food is good, you should eat it");
        classDoc.setFieldDocs(fieldDocs);
        
        classDocs.add(classDoc);
        
        JsonSchemaDocs schemaDocs = new JsonSchemaDocs();
        schemaDocs.setClassDocs(classDocs);
        
        return schemaDocs;
    }

    private class SchemaFieldNamingStrategy implements FieldNamingStrategy
    {
        @Override
        public String translateName(Field field)
        {
            if("enumList".equals(field.getName()))
            {
                return "enum";
            }
            else
            {
                return FieldNamingPolicy.IDENTITY.translateName(field);
            }
        }
    }
}
