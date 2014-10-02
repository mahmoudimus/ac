package com.atlassian.plugin.connect.spi.module.provider;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

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
        return context.deserialize(json, cls);
    }
}
