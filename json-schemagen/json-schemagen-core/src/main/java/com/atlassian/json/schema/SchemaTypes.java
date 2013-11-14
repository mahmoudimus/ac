package com.atlassian.json.schema;

import java.math.BigDecimal;
import java.util.Set;

import com.google.common.collect.*;

public class SchemaTypes
{
    public static final String OBJECT = "object";
    public static final String INTEGER = "integer";
    public static final String NUMBER = "number";
    public static final String BOOLEAN = "boolean";
    public static final String STRING = "string";
    public static final String ARRAY = "array";

    private static final Multimap<String, Class<?>> typeMappings = ArrayListMultimap.create();

    static
    {
        typeMappings.putAll(INTEGER, Lists.newArrayList(byte.class, Byte.class, short.class, Short.class, int.class, Integer.class, long.class, Long.class));
        typeMappings.putAll(NUMBER, Lists.newArrayList(float.class, Float.class, double.class, Double.class, BigDecimal.class));
        typeMappings.putAll(BOOLEAN, Lists.newArrayList(boolean.class, Boolean.class));
        typeMappings.putAll(STRING, Lists.newArrayList(char.class, Character.class, CharSequence.class, String.class));
    }

    public static boolean isMappedType(Class<?> clazz)
    {
        return typeMappings.containsValue(clazz);
    }

    public static String getMappedType(Class<?> clazz)
    {
        Set<String> key = Multimaps.invertFrom(typeMappings, HashMultimap.<Class<?>, String>create()).get(clazz);
        return (null == key || key.isEmpty()) ? null : key.iterator().next();
    }

}
