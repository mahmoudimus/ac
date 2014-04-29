package com.atlassian.json.schema;

import com.atlassian.json.schema.doclet.model.JsonSchemaDocs;
import com.atlassian.json.schema.model.*;
import com.atlassian.json.schema.scanner.model.InterfaceList;
import com.atlassian.json.schema.util.StringUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.*;

import static com.atlassian.json.schema.util.ReflectionUtil.isParameterizedType;

public class DefaultJsonSchemaGenerator extends AbstractJsonSchemaGenerator
{

    public DefaultJsonSchemaGenerator()
    {
        this(EnumCase.UPPER, new InterfaceList(), new JsonSchemaDocs(), null);
    }

    public DefaultJsonSchemaGenerator(EnumCase enumCase, InterfaceList interfaceList, JsonSchemaDocs schemaDocs, String ignoreFilter)
    {
        super(enumCase,interfaceList, schemaDocs, ignoreFilter);
    }

    @Override
    protected JsonSchema generateMapSchema(Class<?> clazz, Field field)
    {
        return new MapTypeSchema();
    }

    @Override
    protected JsonSchema processSimpleType(Class<?> clazz, Field field)
    {
        SimpleTypeSchema simpleSchema;
        SchemaType type = SchemaTypesHelper.getMappedType(clazz);
        switch (type)
        {
            case STRING:
                simpleSchema = new StringSchema();
                addStringAttrsForField((StringSchema) simpleSchema,field);
                break;
            case NUMBER:
            case INTEGER:
                simpleSchema = new NumericSchema(type.name().toLowerCase());
                addNumericAttrsForField((NumericSchema) simpleSchema,field);
                break;
            default:
                simpleSchema = new SimpleTypeSchema(type.name().toLowerCase());
                break;
        }

        addCommonAttrsForField(simpleSchema,field);
        
        return simpleSchema;
    }

    @Override
    protected JsonSchema generateInterfaceSchema(Class<?> clazz, Field field)
    {
        if(!interfaceList.getImplementors(clazz).isEmpty())
        {
            InterfaceSchema schema = new InterfaceSchema();
            Set<ObjectSchema> anyOf = new HashSet<ObjectSchema>();
            
            for(String impl : interfaceList.getImplementors(clazz))
            {
                try
                {
                    Class<?> implClass = Thread.currentThread().getContextClassLoader().loadClass(impl);
                    anyOf.add((ObjectSchema) generateObjectSchema(implClass,null,clazz));
                }
                catch (ClassNotFoundException e)
                {
                    throw new RuntimeException("Unable to find class for interface", e);
                }
            }
            schema.setAnyOf(anyOf);
            
            return schema;
        }
        else
        {
            return generateObjectSchema(clazz,field);
        }
    }

    @Override
    protected JsonSchema generateInterfaceSchemaWithSelfRef(Class<?> clazz, Field field, Class<?> self)
    {
        if(!interfaceList.getImplementors(clazz).isEmpty())
        {
            InterfaceSchema schema = new InterfaceSchema();
            Set<ObjectSchema> anyOf = new HashSet<ObjectSchema>();

            for(String impl : interfaceList.getImplementors(clazz))
            {
                try
                {
                    if(self.getName().equals(impl))
                    {
                        anyOf.add((ObjectSchema) generateSelfRef());
                    }
                    else
                    {
                        Class<?> implClass = Thread.currentThread().getContextClassLoader().loadClass(impl);
                        anyOf.add((ObjectSchema) generateObjectSchema(implClass,null,clazz));
                    }
                }
                catch (ClassNotFoundException e)
                {
                    throw new RuntimeException("Unable to find class for interface", e);
                }
            }
            schema.setAnyOf(anyOf);

            return schema;
        }
        else
        {
            return generateObjectSchema(clazz,field);
        }
    }

    @Override
    protected <T> JsonSchema generateEnumSchema(Class<T> clazz, Field field)
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
            schema.setType(SchemaType.INTEGER.name().toLowerCase());
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
                schema.setType(SchemaType.NUMBER.name().toLowerCase());

            }
            catch (NumberFormatException e1)
            {

                List<String> stringList = new ArrayList<String>();
                for (T constant : clazz.getEnumConstants())
                {
                    switch(enumCase)
                    {
                        case LOWER :
                            stringList.add(constant.toString().toLowerCase());
                            break;
                        
                        case UPPER :
                            stringList.add(constant.toString().toUpperCase());
                            break;
                        
                        case INSENSITIVE :
                            stringList.add(constant.toString().toLowerCase());
                            stringList.add(constant.toString().toUpperCase());
                            break;
                    }
                }

                schema = new EnumSchema<String>();
                schema.setEnumList(stringList);
                schema.setType(SchemaType.STRING.name().toLowerCase());
            }
        }

        addCommonAttrsForField(schema,field);

        return schema;
    }

    @Override
    protected JsonSchema generateArraySchema(Class<?> owner, Field field, String defaultArrayTitle, Class... ifaces)
    {
        ArrayTypeSchema schema = new ArrayTypeSchema();
        
        if(null != field)
        {
            addCommonAttrsForField(schema,field);
            addArrayAttrsForField(schema,field);
            
            if (isParameterizedType(field.getGenericType()))
            {
                ParameterizedType ptype = (ParameterizedType) field.getGenericType();
                Class<?> listType = (Class<?>) ptype.getActualTypeArguments()[0];
                
                if(listType.isInterface() && Arrays.asList(ifaces).contains(listType))
                {
                    schema.setItems(generateInterfaceSchemaWithSelfRef(listType,null,owner));
                }
                else
                {
                    JsonSchema listTypeSchema = generateSchemaForClass(listType, null);
                    if(StringUtil.isBlank(listTypeSchema.getTitle()))
                    {
                        listTypeSchema.setTitle(defaultArrayTitle);
                    }
                    schema.setItems(listTypeSchema);
                }
            }
        }

        return schema;
    }

    @Override
    protected JsonSchema generateSchemaForField(Class<?> owner, Field field, Class<?>[] ifaces, String defaultArrayTitle)
    {
        if (Collection.class.isAssignableFrom(field.getType()))
        {
            return generateArraySchema(owner, field, defaultArrayTitle, ifaces);
        }
        else if(Arrays.asList(ifaces).contains(field.getType()))
        {
            return generateSelfRef();
        }
        else
        {
            return generateSchemaForClass(field.getType(),field);
        }
    }

    public JsonSchema generateSelfRef()
    {
        ObjectSchema refSchema = new ObjectSchema();
        refSchema.setRef("#");
        
        return refSchema;
    }

}
