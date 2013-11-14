package com.atlassian.json.schema;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtil
{
    public static List<Field> getPropertiesForJson(Class<?> clazz)
    {
        List<Field> fieldsForJson = new ArrayList<Field>();

        for (Field field : clazz.getDeclaredFields())
        {
            int mods = field.getModifiers();

            if (!Modifier.isAbstract(mods) && !Modifier.isStatic(mods) && !Modifier.isTransient(mods))
            {
                field.setAccessible(true);
                fieldsForJson.add(field);
            }
        }

        return fieldsForJson;
    }

    public static boolean isParameterizedType(Type type)
    {
        return (type instanceof ParameterizedType);
    }
}
