package com.atlassian.plugin.connect.plugin.capabilities.gson;

import com.atlassian.plugin.connect.api.scopes.ScopeName;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Arrays;

public class ScopeNameSerializer implements JsonSerializer<ScopeName>, JsonDeserializer<ScopeName>
{
    @Override
    public JsonElement serialize(ScopeName scopeName, Type type, JsonSerializationContext jsonSerializationContext)
    {
        return new JsonPrimitive(null == scopeName ? null : scopeName.toString());
    }

    @Override
    public ScopeName deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException
    {
        String scopeNameStr = null == jsonElement ? null : jsonElement.getAsString();

        try
        {
            return ScopeName.valueOf(scopeNameStr);
        }
        catch (IllegalArgumentException e)
        {
            throw new InvalidDescriptorException(String.format("Scope name '%s' is not valid. Valid scopes are %s", scopeNameStr, Arrays.asList(ScopeName.values())));
        }
    }
}
