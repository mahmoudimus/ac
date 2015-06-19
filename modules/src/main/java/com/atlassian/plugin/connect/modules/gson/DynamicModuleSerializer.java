package com.atlassian.plugin.connect.modules.gson;

import com.google.gson.*;

import java.lang.reflect.Type;

public class DynamicModuleSerializer implements JsonDeserializer<JsonObject>
{
    @Override
    public JsonObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException
    {
        return json.getAsJsonObject();
        
    }
}
