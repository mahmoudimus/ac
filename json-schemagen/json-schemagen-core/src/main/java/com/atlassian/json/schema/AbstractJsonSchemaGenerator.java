package com.atlassian.json.schema;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.json.schema.doclet.model.JsonSchemaDocs;
import com.atlassian.json.schema.doclet.model.SchemaClassDoc;
import com.atlassian.json.schema.doclet.model.SchemaFieldDoc;
import com.atlassian.json.schema.model.ArrayTypeSchema;
import com.atlassian.json.schema.model.BasicSchema;
import com.atlassian.json.schema.model.JsonSchema;
import com.atlassian.json.schema.model.ObjectSchema;
import com.atlassian.json.schema.scanner.model.InterfaceList;
import com.atlassian.json.schema.util.ReflectionUtil;

import static com.atlassian.json.schema.util.StringUtil.isNotBlank;
import static com.atlassian.json.schema.util.StringUtil.lowerCamel;

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
    public JsonSchema generateSchema(Class<?> rootClass)
    {
        BasicSchema rootSchema = (BasicSchema) generateSchemaForClass(rootClass);
        rootSchema.addDollarSchema();

        return rootSchema;
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

    protected JsonSchema generateObjectSchema(Class<?> clazz, Class<?>... ifaces)
    {
        ObjectSchema schema = new ObjectSchema();
        schema.setId(lowerCamel(clazz.getSimpleName()));

        addDocsForClass(schema, clazz);

        Map<String, Object> props = new HashMap<String, Object>();

        for (Field field : ReflectionUtil.getPropertiesForJson(clazz))
        {
            JsonSchema fieldSchema = generateSchemaForField(clazz, field, ifaces);

            if (null != fieldSchema)
            {
                addDocsForField(fieldSchema, clazz, field);
                props.put(field.getName(), fieldSchema);
            }
        }

        schema.setProperties(props);

        return schema;
    }

    protected void addDocsForClass(JsonSchema schema, Class<?> theClass)
    {
        SchemaClassDoc classDoc = schemaDocs.getClassDoc(theClass.getName());

        if (null != classDoc)
        {
            if (isNotBlank(classDoc.getClassTitle()))
            {
                schema.setTitle(classDoc.getClassTitle());
            }

            if (isNotBlank(classDoc.getClassDoc()))
            {
                schema.setDescription(classDoc.getClassDoc());
            }
        }
    }

    protected void addDocsForField(JsonSchema schema, Class<?> theClass, Field theField)
    {
        SchemaClassDoc classDoc = schemaDocs.getClassDoc(theClass.getName());

        if (null != classDoc)
        {
            SchemaFieldDoc fieldDoc = classDoc.getFieldDoc(theField.getName());

            if (null != fieldDoc)
            {
                if (isNotBlank(fieldDoc.getFieldTitle()))
                {
                    schema.setTitle(fieldDoc.getFieldTitle());
                }

                if (isNotBlank(fieldDoc.getFieldDocs()))
                {
                    schema.setDescription(fieldDoc.getFieldDocs());
                }
            }
        }
    }

    protected abstract JsonSchema generateSchemaForField(Class<?> clazz, Field field, Class<?>[] ifaces);

    protected abstract JsonSchema generateInterfaceSchema(Class<?> clazz);

    protected abstract <T> JsonSchema generateEnumSchema(Class<T> clazz);

    protected abstract JsonSchema generateMapSchema(Class<?> clazz);

    protected abstract JsonSchema processSimpleType(Class<?> clazz);

    protected abstract JsonSchema generateArraySchema(Field field, Class... ifaces);
}
