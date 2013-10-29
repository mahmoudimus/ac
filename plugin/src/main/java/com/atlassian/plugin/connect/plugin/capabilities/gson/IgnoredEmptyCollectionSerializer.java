package com.atlassian.plugin.connect.plugin.capabilities.gson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConditionalBean;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class IgnoredEmptyCollectionSerializer implements JsonSerializer<Collection<?>>
{

    @Override
    public JsonElement serialize(Collection<?> src, Type typeOfSrc, JsonSerializationContext context)
    {
        if(null == src || src.isEmpty())
        {
            return null;
        }
        
        JsonArray array = new JsonArray();
        for(Object entry : src)
        {
            array.add(context.serialize(entry));
        }
        
        return array;
    }
}
