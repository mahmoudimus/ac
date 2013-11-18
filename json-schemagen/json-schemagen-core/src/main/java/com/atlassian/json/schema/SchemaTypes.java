package com.atlassian.json.schema;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaTypes
{
    public static final String OBJECT = "object";
    public static final String INTEGER = "integer";
    public static final String NUMBER = "number";
    public static final String BOOLEAN = "boolean";
    public static final String STRING = "string";
    public static final String ARRAY = "array";

    private static final Map<String, List<Class<?>>> jsonToJava = createJsonToJava();

    private static Map<String, List<Class<?>>> createJsonToJava()
    {
        Map<String, List<Class<?>>> jsonToJava = new HashMap<String, List<Class<?>>>();
        List<Class<?>> intList = Arrays.<Class<?>>asList(byte.class, Byte.class, short.class, Short.class, int.class, Integer.class, long.class, Long.class);
        List<Class<?>> numList = Arrays.<Class<?>>asList(float.class, Float.class, double.class, Double.class, BigDecimal.class);
        List<Class<?>> boolList = Arrays.<Class<?>>asList(boolean.class, Boolean.class);
        List<Class<?>> strList = Arrays.<Class<?>>asList(char.class, Character.class, CharSequence.class, String.class);

        jsonToJava.put(INTEGER, intList);
        jsonToJava.put(NUMBER, numList);
        jsonToJava.put(BOOLEAN, boolList);
        jsonToJava.put(STRING, strList);

        return jsonToJava;

    }

    public static boolean isMappedType(Class<?> clazz)
    {
        for (Map.Entry<String, List<Class<?>>> entry : jsonToJava.entrySet())
        {
            if (entry.getValue().contains(clazz))
            {
                return true;
            }
        }

        return false;
    }

    public static String getMappedType(Class<?> clazz)
    {
        for (Map.Entry<String, List<Class<?>>> entry : jsonToJava.entrySet())
        {
            if (entry.getValue().contains(clazz))
            {
                return entry.getKey();
            }
        }

        return null;
    }

}
