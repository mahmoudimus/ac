package com.atlassian.plugin.connect.spi.module.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class DefaultModuleDeserialiser implements ModuleDeserialiser
{
    private final JsonDeserializationContext context;
    private final JsonElement json;

    public DefaultModuleDeserialiser(JsonDeserializationContext context, JsonElement json)
    {
        this.context = context;
        this.json = json;
    }

    @Override
    public Object deserialise(Class<?> cls)
    {
        if (json.isJsonArray())
        {
            List<Object> l = newArrayList();
            for (JsonElement jsonElement : json.getAsJsonArray())
            {
                l.add(context.deserialize(jsonElement, cls));
            }

            return l;
        }
        return context.deserialize(json, cls);
    }
}
