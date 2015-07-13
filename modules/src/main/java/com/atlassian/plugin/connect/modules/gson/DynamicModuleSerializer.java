package com.atlassian.plugin.connect.modules.gson;

import com.google.gson.*;

import java.lang.reflect.Type;

public class DynamicModuleSerializer implements JsonDeserializer<JsonObject>, JsonSerializer<JsonObject>
{
    @Override
    public JsonObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException
    {
        return json.getAsJsonObject();
        
    }
    
    @Override
    public JsonElement serialize(JsonObject object, Type typeOfT, JsonSerializationContext context)
    {
        return object;
    }
}