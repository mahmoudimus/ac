package com.atlassian.plugin.connect.plugin.capabilities.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class ConnectReflectionHelper
{
    public static boolean isParameterizedListWithType(Type type, Class typeParam)
    {
        return (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType().equals(List.class) && typeParam.isAssignableFrom(((ParameterizedType) type).getActualTypeArguments()[0].getClass()));
    }

    public static boolean isParameterizedList(Type type)
    {
        return (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType().equals(List.class));
    }
}
