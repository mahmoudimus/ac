package com.atlassian.json.schema;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.*;

import com.atlassian.json.schema.model.*;
import com.atlassian.json.schema.scanner.model.InterfaceList;

import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static com.atlassian.json.schema.ReflectionUtil.isParameterizedType;

public class JsonSchemaGenerator
{
    private final boolean lowercaseEnums;
    private InterfaceList interfaceList;

    public JsonSchemaGenerator()
    {
        this(false, new InterfaceList());
    }

    JsonSchemaGenerator(boolean lowercaseEnums, InterfaceList interfaceList)
    {
        this.lowercaseEnums = lowercaseEnums;
        this.interfaceList = interfaceList;
    }

    public String generateSchema(Class<?> rootClass)
    {
        RootSchema rootSchema = (RootSchema) generateSchemaForClass(rootClass);
        rootSchema.addDollarSchema();

        GsonBuilder gb = new GsonBuilder();
        gb.setPrettyPrinting();

        Gson gson = gb.create();

        return gson.toJson(rootSchema);
    }

    public JsonSchema generateSchemaForClass(Class<?> clazz)
    {
        if (SchemaTypes.isMappedType(clazz))
        {
            return new SimpleTypeSchema(SchemaTypes.getMappedType(clazz));
        }

        if (Map.class.isAssignableFrom(clazz))
        {
            return new MapTypeSchema();
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

    private JsonSchema generateInterfaceSchema(Class<?> clazz)
    {
        if(!interfaceList.getImplementors(clazz).isEmpty())
        {
            InterfaceSchema schema = new InterfaceSchema();
            List<ObjectSchema> anyOf = new ArrayList<ObjectSchema>();
            
            for(String impl : interfaceList.getImplementors(clazz))
            {
                try
                {
                    Class<?> implClass = Class.forName(impl);
                    anyOf.add((ObjectSchema) generateObjectSchema(implClass,clazz));
                }
                catch (ClassNotFoundException e)
                {
                    //TODO: should we throw or ignore?
                    e.printStackTrace();
                }
            }
            schema.setAnyOf(anyOf);
            
            return schema;
        }
        else
        {
            return generateObjectSchema(clazz);
        }
    }

    private <T> JsonSchema generateEnumSchema(Class<T> clazz)
    {
        String value = clazz.getEnumConstants()[0].toString();
        EnumSchema schema;
        try
        {
            Long.parseLong(value);
            List<Long> intList = new ArrayList<Long>();
            for (T constant : clazz.getEnumConstants())
            {
                intList.add(Long.parseLong(constant.toString()));
            }

            schema = new EnumSchema<Long>();
            schema.setEnumList(intList);
            schema.setType(SchemaTypes.INTEGER);
        }
        catch (NumberFormatException e)
        {
            try
            {
                BigDecimal number = new BigDecimal(value);
                List<BigDecimal> floatList = new ArrayList<BigDecimal>();

                for (T constant : clazz.getEnumConstants())
                {
                    floatList.add(new BigDecimal(constant.toString()));
                }

                schema = new EnumSchema<BigDecimal>();
                schema.setEnumList(floatList);
                schema.setType(SchemaTypes.NUMBER);

            }
            catch (NumberFormatException e1)
            {

                List<String> stringList = new ArrayList<String>();
                for (T constant : clazz.getEnumConstants())
                {
                    if (lowercaseEnums)
                    {
                        stringList.add(constant.toString().toLowerCase());
                    }
                    else
                    {
                        stringList.add(constant.toString());
                    }
                }

                schema = new EnumSchema<String>();
                schema.setEnumList(stringList);
                schema.setType(SchemaTypes.STRING);
            }
        }

        return schema;
    }

    private JsonSchema generateArraySchema(Field field, Class... ifaces)
    {
        ArrayTypeSchema schema = new ArrayTypeSchema();
        if (isParameterizedType(field.getGenericType()))
        {
            ParameterizedType ptype = (ParameterizedType) field.getGenericType();
            Class<?> listType = (Class<?>) ptype.getActualTypeArguments()[0];
            
            if(listType.isInterface() && Arrays.asList(ifaces).contains(listType))
            {
                schema.setItems(generateSelfRef());
            }
            else
            {
                schema.setItems(generateSchemaForClass(listType));
            }
        }

        return schema;
    }

    private JsonSchema generateObjectSchema(Class<?> clazz, Class<?> ... ifaces)
    {
        ObjectSchema schema = new ObjectSchema();
        schema.setId(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, clazz.getSimpleName()));

        Map<String, Object> props = new HashMap<String, Object>();

        for (Field field : ReflectionUtil.getPropertiesForJson(clazz))
        {
            if (Collection.class.isAssignableFrom(field.getType()))
            {
                props.put(field.getName(), generateArraySchema(field, ifaces));
            }
            else if(Arrays.asList(ifaces).contains(field.getType()))
            {
                props.put(field.getName(),generateSelfRef());
            }
            else
            {
                props.put(field.getName(), generateSchemaForClass(field.getType()));
            }
        }

        schema.setProperties(props);

        return schema;
    }
    
    public JsonSchema generateSelfRef()
    {
        RootSchema refSchema = new RootSchema();
        refSchema.setRef("#");
        
        return refSchema;
    }

}
