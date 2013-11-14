package com.atlassian.json.schema;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.json.schema.model.*;

import com.google.common.base.CaseFormat;

import static com.atlassian.json.schema.ReflectionUtil.isParameterizedType;

public class DefaultSchemaFactory implements SchemaFactory
{
    @Override
    public JsonSchema generateSchema(Class<?> clazz)
    {
        if(SchemaTypes.isMappedType(clazz))
        {
            return new SimpleTypeSchema(SchemaTypes.getMappedType(clazz));
        }
        
        if(Map.class.isAssignableFrom(clazz))
        {
            return new MapTypeSchema();
        }

        if(Collection.class.isAssignableFrom(clazz))
        {
            return new ArrayTypeSchema();
        }
        
        return generateObjectSchema(clazz);
    }

    private JsonSchema generateArraySchema(Field field)
    {
        ArrayTypeSchema schema = new ArrayTypeSchema();
        if(isParameterizedType(field.getGenericType()))
        {
            ParameterizedType ptype = (ParameterizedType) field.getGenericType();
            Class<?> listType = (Class<?>) ptype.getActualTypeArguments()[0];
            schema.setItems(generateSchema(listType));
        }
        
        return schema;
    }

    private JsonSchema generateObjectSchema(Class<?> clazz)
    {
        ObjectSchema schema = new ObjectSchema();
        schema.setId(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, clazz.getSimpleName()));

        Map<String,Object> props = new HashMap<String, Object>();
        
        for(Field field : ReflectionUtil.getPropertiesForJson(clazz))
        {
            if(Collection.class.isAssignableFrom(field.getType()))
            {
                props.put(field.getName(), generateArraySchema(field));
            }
            else
            {
                props.put(field.getName(), generateSchema(field.getType()));
            }
        }
        
        schema.setProperties(props);
        
        return schema;
    }
}
