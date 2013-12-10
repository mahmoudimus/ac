package com.atlassian.json.schema;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaTypesHelper
{
    private static final Map<SchemaType, List<Class<?>>> jsonToJava = createJsonToJava();

    private static Map<SchemaType, List<Class<?>>> createJsonToJava()
    {
        Map<SchemaType, List<Class<?>>> jsonToJava = new HashMap<SchemaType, List<Class<?>>>();
        List<Class<?>> intList = Arrays.<Class<?>>asList(byte.class, Byte.class, short.class, Short.class, int.class, Integer.class, long.class, Long.class);
        List<Class<?>> numList = Arrays.<Class<?>>asList(float.class, Float.class, double.class, Double.class, BigDecimal.class);
        List<Class<?>> boolList = Arrays.<Class<?>>asList(boolean.class, Boolean.class);
        List<Class<?>> strList = Arrays.<Class<?>>asList(char.class, Character.class, CharSequence.class, String.class);

        jsonToJava.put(SchemaType.INTEGER, intList);
        jsonToJava.put(SchemaType.NUMBER, numList);
        jsonToJava.put(SchemaType.BOOLEAN, boolList);
        jsonToJava.put(SchemaType.STRING, strList);

        return jsonToJava;

    }

    public static boolean isMappedType(Class<?> clazz)
    {
        for (Map.Entry<SchemaType, List<Class<?>>> entry : jsonToJava.entrySet())
        {
            if (entry.getValue().contains(clazz))
            {
                return true;
            }
        }

        return false;
    }

    public static SchemaType getMappedType(Class<?> clazz)
    {
        for (Map.Entry<SchemaType, List<Class<?>>> entry : jsonToJava.entrySet())
        {
            if (entry.getValue().contains(clazz))
            {
                return entry.getKey();
            }
        }

        return null;
    }

}
