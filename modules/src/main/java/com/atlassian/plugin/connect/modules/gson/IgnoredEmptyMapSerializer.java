package com.atlassian.plugin.connect.modules.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Map;

public class IgnoredEmptyMapSerializer implements JsonSerializer<Map<String, String>>
{

    @Override
    public JsonElement serialize(Map<String, String> src, Type typeOfSrc, JsonSerializationContext context)
    {
        if (null == src || src.isEmpty())
        {
            return null;
        }

        JsonObject job = new JsonObject();
        for (Map.Entry<String, String> entry : src.entrySet())
        {
            job.addProperty(entry.getKey(), entry.getValue());
        }

        return job;
    }
}

