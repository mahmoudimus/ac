package com.atlassian.plugin.connect.plugin.rest.data;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.util.*;

public abstract class RestMapEntity extends LinkedHashMap<String, Object>
{
    protected static <F, T> List<T> transform(Iterable<F> values, Function<F, T> transformer)
    {
        return ImmutableList.copyOf(Iterables.transform(values, transformer));
    }

    protected static <F, T> Set<T> transform(Set<F> values, Function<F, T> transformer)
    {
        return ImmutableSet.copyOf(Collections2.transform(values, transformer));
    }

    protected <T extends Enum<T>> T getEnumProperty(String property, Class<T> enumClass)
    {
        Object value = get(property);
        if (enumClass.isInstance(value))
        {
            return enumClass.cast(value);
        }
        else if (value instanceof String)
        {
            return Enum.valueOf(enumClass, ((String) value).toUpperCase(Locale.US));
        }
        return null;
    }

    protected int getIntProperty(String property)
    {
        Object value = get(property);
        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        else if (value instanceof String)
        {
            return Integer.valueOf((String) value);
        }
        return -1;
    }

    protected boolean getBoolProperty(String property)
    {
        return getBoolProperty(property, false);
    }

    protected boolean getBoolProperty(String property, boolean defaultValue)
    {
        Object value = get(property);
        if (value instanceof Boolean)
        {
            return (Boolean) value;
        }
        else if (value instanceof String)
        {
            return Boolean.valueOf((String) value);
        }
        return defaultValue;
    }

    protected long getLongProperty(String property)
    {
        Object value = get(property);
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        else if (value instanceof String)
        {
            return Long.valueOf((String) value);
        }
        return -1;
    }

    protected String getStringProperty(String property)
    {
        return (String) get(property);
    }

    protected void putIfNotEmpty(String key, Collection<?> value)
    {
        if (!(value == null || value.isEmpty()))
        {
            put(key, value);
        }
    }

    protected void putIfNotEmpty(String key, Iterable<?> value)
    {
        if (!(value == null || Iterables.isEmpty(value)))
        {
            put(key, value);
        }
    }

    protected void putIfNotEmpty(String key, Map<?, ?> value)
    {
        if (!(value == null || value.isEmpty()))
        {
            put(key, value);
        }
    }

    protected void putIfNotNull(String key, Object value)
    {
        if (value != null)
        {
            put(key, value);
        }
    }
}
