package com.atlassian.json.schema;

import com.atlassian.json.schema.annotation.*;
import com.atlassian.json.schema.doclet.model.JsonSchemaDocs;
import com.atlassian.json.schema.doclet.model.SchemaClassDoc;
import com.atlassian.json.schema.doclet.model.SchemaFieldDoc;
import com.atlassian.json.schema.model.*;
import com.atlassian.json.schema.scanner.model.InterfaceList;
import com.atlassian.json.schema.util.ReflectionUtil;
import com.atlassian.json.schema.util.StringUtil;

import java.lang.reflect.Field;
import java.util.*;

import static com.atlassian.json.schema.util.StringUtil.isNotBlank;
import static com.atlassian.json.schema.util.StringUtil.lowerCamel;

public abstract class AbstractJsonSchemaGenerator implements JsonSchemaGenerator
{
    protected final boolean lowercaseEnums;
    protected final InterfaceList interfaceList;
    protected final JsonSchemaDocs schemaDocs;
    protected final String ignoreFilter;

    protected AbstractJsonSchemaGenerator(boolean lowercaseEnums, InterfaceList interfaceList, JsonSchemaDocs schemaDocs, String ignoreFilter)
    {
        this.lowercaseEnums = lowercaseEnums;
        this.interfaceList = interfaceList;
        this.schemaDocs = schemaDocs;
        
        if(null == ignoreFilter)
        {
            this.ignoreFilter = "";
        }
        else
        {
            this.ignoreFilter = ignoreFilter;
        }
    }

    @Override
    public JsonSchema generateSchema(Class<?> rootClass)
    {
        BasicSchema rootSchema = (BasicSchema) generateSchemaForClass(rootClass,null);
        rootSchema.addDollarSchema();

        return rootSchema;
    }

    protected JsonSchema generateSchemaForClass(Class<?> clazz, Field field)
    {
        if (SchemaTypesHelper.isMappedType(clazz))
        {
            return processSimpleType(clazz, field);
        }

        if (Map.class.isAssignableFrom(clazz))
        {
            return generateMapSchema(clazz, field);
        }

        if (Collection.class.isAssignableFrom(clazz))
        {
            return generateArraySchema(clazz, field, null);
        }

        if (clazz.isEnum())
        {
            return generateEnumSchema(clazz, field);
        }

        if (clazz.isInterface())
        {
            return generateInterfaceSchema(clazz, field);
        }

        return generateObjectSchema(clazz, field);
    }

    protected JsonSchema generateObjectSchema(Class<?> clazz, Field field, Class<?>... ifaces)
    {
        ObjectSchema schema = new ObjectSchema();
        schema.setId(lowerCamel(clazz.getSimpleName()));
        
        addCommonAttrsForField(schema,field);
        addObjectAttrsForClass(schema, clazz);
        
        addDocsForClass(schema, clazz);

        Map<String, Object> props = new HashMap<String, Object>();

        for (Field propField : ReflectionUtil.getPropertiesForJson(clazz))
        {
            if(propField.isAnnotationPresent(SchemaIgnore.class))
            {
                String fieldIgnoreFilter = propField.getAnnotation(SchemaIgnore.class).value();
                
                if(StringUtil.isBlank(fieldIgnoreFilter) || ignoreFilter.equals(fieldIgnoreFilter))
                {
                    continue;
                }
            }
            
            String defaultArrayTitle = getFieldTitle(clazz,propField);
            JsonSchema fieldSchema = generateSchemaForField(clazz, propField, ifaces, defaultArrayTitle);

            if (null != fieldSchema)
            {
                addDocsForField(fieldSchema, clazz, propField);
                props.put(getFieldName(propField), fieldSchema);
            }
            
            if(propField.isAnnotationPresent(Required.class))
            {
                if(null == schema.getRequired())
                {
                    HashSet<String> required = new HashSet<String>();
                    schema.setRequired(required);
                }
                
                schema.getRequired().add(propField.getName());
            }
        }

        schema.setProperties(props);

        return schema;
    }

    protected String getFieldName(Field field)
    {
        return field.getName();
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
                    schema.setFieldTitle(fieldDoc.getFieldTitle());
                }

                if (isNotBlank(fieldDoc.getFieldDocs()))
                {
                    schema.setFieldDescription(fieldDoc.getFieldDocs());
                }
            }
        }
    }

    protected String getFieldTitle(Class<?> theClass, Field theField)
    {
        SchemaClassDoc classDoc = schemaDocs.getClassDoc(theClass.getName());
        String title = null;
        if (null != classDoc)
        {
            SchemaFieldDoc fieldDoc = classDoc.getFieldDoc(theField.getName());

            if (null != fieldDoc)
            {
                if (isNotBlank(fieldDoc.getFieldTitle()))
                {
                    title = fieldDoc.getFieldTitle();
                }
            }
        }
        
        if(null == title)
        {
            if(theField.isAnnotationPresent(CommonSchemaAttributes.class))
            {
                title = theField.getAnnotation(CommonSchemaAttributes.class).title();
            }
        }
        
        return title;
    }

    protected void addCommonAttrsForField(AbstractSchema schema, Field theField)
    {
        if(null == theField)
        {
            return;
        }
        
        if(theField.isAnnotationPresent(CommonSchemaAttributes.class))
        {
            CommonSchemaAttributes commonAnno = theField.getAnnotation(CommonSchemaAttributes.class);
            
            if(StringUtil.isNotBlank(commonAnno.defaultValue()))
            {
                schema.setDefaultValue(commonAnno.defaultValue());
            }
            if(StringUtil.isNotBlank(commonAnno.title()))
            {
                schema.setTitle(commonAnno.title());
            }
            if(StringUtil.isNotBlank(commonAnno.description()))
            {
                schema.setDescription(commonAnno.description());
            }
            if(commonAnno.allOf().length > 0)
            {
                schema.setAllOf(generateSchemaSet(commonAnno.allOf()));
            }
            if(commonAnno.anyOf().length > 0)
            {
                schema.setAnyOf(generateSchemaSet(commonAnno.anyOf()));
            }
            if(commonAnno.oneOf().length > 0)
            {
                schema.setOneOf(generateSchemaSet(commonAnno.oneOf()));
            }
            if(!AnnotationHelper.EmptyClass.class.getName().equals(commonAnno.not().getName()))
            {
                schema.setNot((ObjectSchema) generateObjectSchema(commonAnno.not(),null));
            }
            
            if(commonAnno.definitions().length > 0)
            {
                //TODO: implement definition/ref handling
            }
        }
    }

    protected void addStringAttrsForField(StringSchema schema, Field theField)
    {
        if(null == theField)
        {
            return;
        }

        if(theField.isAnnotationPresent(StringSchemaAttributes.class))
        {
            StringSchemaAttributes stringAnno = theField.getAnnotation(StringSchemaAttributes.class);

            if(StringUtil.isNotBlank(stringAnno.format()))
            {
                schema.setFormat(stringAnno.format());
            }
            if(StringUtil.isNotBlank(stringAnno.pattern()))
            {
                schema.setPattern(stringAnno.pattern());
            }
            if(stringAnno.maxLength() != Integer.MAX_VALUE)
            {
                schema.setMaxLength(stringAnno.maxLength());
            }
            if(stringAnno.minLength() != Integer.MIN_VALUE)
            {
                schema.setMinLength(stringAnno.minLength());
            }
        }
    }

    protected void addNumericAttrsForField(NumericSchema schema, Field theField)
    {
        if(null == theField)
        {
            return;
        }

        if(theField.isAnnotationPresent(NumericSchemaAttributes.class))
        {
            NumericSchemaAttributes numericAnno = theField.getAnnotation(NumericSchemaAttributes.class);

            if(numericAnno.maximum() != Double.MAX_VALUE)
            {
                schema.setMaximum(numericAnno.maximum());
                schema.setExclusiveMaximum(numericAnno.exclusiveMaximum());
            }
            if(numericAnno.minimum() != Double.MIN_VALUE)
            {
                schema.setMaximum(numericAnno.maximum());
                schema.setExclusiveMinimum(numericAnno.exclusiveMinimum());
            }
            if(numericAnno.multipleOf() != -1)
            {
                schema.setMultipleOf(numericAnno.multipleOf());
            }
        }
    }

    protected void addArrayAttrsForField(ArrayTypeSchema schema, Field theField)
    {
        if(null == theField)
        {
            return;
        }

        if(theField.isAnnotationPresent(ArraySchemaAttributes.class))
        {
            ArraySchemaAttributes arrayAnno = theField.getAnnotation(ArraySchemaAttributes.class);
            
            if(arrayAnno.maxItems() != Integer.MAX_VALUE)
            {
                schema.setMaxItems(arrayAnno.maxItems());
            }
            if(arrayAnno.minItems() != Integer.MIN_VALUE)
            {
                schema.setMinItems(arrayAnno.minItems());
            }
            
            schema.setAdditionalItems(arrayAnno.additionalItems());
            schema.setUniqueItems(arrayAnno.uniqueItems());
        }
    }

    protected void addObjectAttrsForClass(ObjectSchema schema, Class theClass)
    {
        if(null == theClass)
        {
            return;
        }

        if(theClass.isAnnotationPresent(ObjectSchemaAttributes.class))
        {
            ObjectSchemaAttributes objAnno = (ObjectSchemaAttributes) theClass.getAnnotation(ObjectSchemaAttributes.class);

            if(objAnno.maxProperties() != Integer.MAX_VALUE)
            {
                schema.setMaxProperties(objAnno.maxProperties());
            }
            if(objAnno.minProperties() != Integer.MIN_VALUE)
            {
                schema.setMinProperties(objAnno.minProperties());
            }
            if(objAnno.patternProperties().length > 0)
            {
                Set<String> patternProps = new HashSet<String>(objAnno.patternProperties().length);
                for(String pattern : objAnno.patternProperties())
                {
                    patternProps.add(pattern);
                }
                
                schema.setPatternProperties(patternProps);
            }
            
            schema.setAdditionalProperties(objAnno.additionalProperties());
            
            if(objAnno.dependencies().length > 0)
            {
                Map<String,Set<String>> deps = new HashMap<String, Set<String>>(objAnno.dependencies().length);
                
                for(SchemaDependency dep : objAnno.dependencies())
                {
                    deps.put(dep.property(),new HashSet<String>(Arrays.asList(dep.requires())));
                }
                schema.setDependencies(deps);
            }
        }
    }
    
    protected Set<ObjectSchema> generateSchemaSet(Class[] classes)
    {
        Set<ObjectSchema> schemas = new HashSet<ObjectSchema>(classes.length);
        for(Class clazz : classes)
        {
            if(clazz.isInterface())
            {
                schemas.add((ObjectSchema) generateInterfaceSchema(clazz,null));
            }
            else
            {
                schemas.add((ObjectSchema) generateObjectSchema(clazz,null));
            }
        }
        
        return schemas;
    }

    protected abstract JsonSchema generateSchemaForField(Class<?> clazz, Field field, Class<?>[] ifaces, String defaultArrayTitle);

    protected abstract JsonSchema generateInterfaceSchema(Class<?> clazz, Field field);

    protected abstract JsonSchema generateInterfaceSchemaWithSelfRef(Class<?> clazz, Field field, Class<?> self);

    protected abstract <T> JsonSchema generateEnumSchema(Class<T> clazz, Field field);

    protected abstract JsonSchema generateMapSchema(Class<?> clazz, Field field);

    protected abstract JsonSchema processSimpleType(Class<?> clazz, Field field);

    protected abstract JsonSchema generateArraySchema(Class<?> clazz, Field field, String defaultArrayTitle, Class... ifaces);
}
