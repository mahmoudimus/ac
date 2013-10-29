package com.atlassian.plugin.connect.plugin.capabilities.gson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import com.google.gson.*;

public class IgnoredEmptyMapSerializer implements JsonSerializer<Map<String,String>>
{

    @Override
    public JsonElement serialize(Map<String,String> src, Type typeOfSrc, JsonSerializationContext context)
    {
        if(null == src || src.isEmpty())
        {
            return null;
        }

        ParameterizedType deserializationCollection = ((ParameterizedType) typeOfSrc);
        Type collectionItemType = deserializationCollection.getActualTypeArguments()[0];

        JsonObject job = new JsonObject();
        for(Map.Entry<String,String> entry : src.entrySet())
        {
            job.addProperty(entry.getKey(),entry.getValue());
        }

        return job;
    }
}

