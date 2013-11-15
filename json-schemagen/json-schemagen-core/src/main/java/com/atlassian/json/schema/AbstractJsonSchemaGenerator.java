package com.atlassian.json.schema;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.json.schema.doclet.model.JsonSchemaDocs;
import com.atlassian.json.schema.model.*;
import com.atlassian.json.schema.scanner.model.InterfaceList;

import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class AbstractJsonSchemaGenerator implements JsonSchemaGenerator
{
    protected final boolean lowercaseEnums;
    protected final InterfaceList interfaceList;
    protected final JsonSchemaDocs schemaDocs;

    protected AbstractJsonSchemaGenerator(boolean lowercaseEnums, InterfaceList interfaceList, JsonSchemaDocs schemaDocs)
    {
        this.lowercaseEnums = lowercaseEnums;
        this.interfaceList = interfaceList;
        this.schemaDocs = schemaDocs;
    }

    @Override
    public String generateSchema(Class<?> rootClass)
    {
        BasicSchema rootSchema = (BasicSchema) generateSchemaForClass(rootClass);
        rootSchema.addDollarSchema();

        GsonBuilder gb = new GsonBuilder();
        gb.setPrettyPrinting();

        Gson gson = gb.create();

        return gson.toJson(rootSchema);
    }

    protected JsonSchema generateSchemaForClass(Class<?> clazz)
    {
        if (SchemaTypes.isMappedType(clazz))
        {
            return processSimpleType(clazz);
        }

        if (Map.class.isAssignableFrom(clazz))
        {
            return generateMapSchema(clazz);
        }

        if (Collection.class.isAssignableFrom(clazz))
        {
            return new ArrayTypeSchema();
        }

        if (clazz.isEnum())
        {
            return generateEnumSchema(clazz);
        }

        if (clazz.isInterface())
        {
            return generateInterfaceSchema(clazz);
        }

        return generateObjectSchema(clazz);
    }

    protected JsonSchema generateObjectSchema(Class<?> clazz, Class<?> ... ifaces)
    {
        ObjectSchema schema = new ObjectSchema();
        schema.setId(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, clazz.getSimpleName()));

        Map<String, Object> props = new HashMap<String, Object>();

        for (Field field : ReflectionUtil.getPropertiesForJson(clazz))
        {
            JsonSchema fieldSchema = generateSchemaForField(clazz,field,ifaces);
            
            if(null != fieldSchema)
            {
                props.put(field.getName(),fieldSchema);
            }
        }

        schema.setProperties(props);

        return schema;
    }

    protected abstract JsonSchema generateSchemaForField(Class<?> clazz, Field field, Class<?>[] ifaces);

    protected abstract JsonSchema generateInterfaceSchema(Class<?> clazz);

    protected abstract <T> JsonSchema generateEnumSchema(Class<T> clazz);

    protected abstract JsonSchema generateMapSchema(Class<?> clazz);

    protected abstract JsonSchema processSimpleType(Class<?> clazz);

    protected abstract JsonSchema generateArraySchema(Field field, Class... ifaces);
}
