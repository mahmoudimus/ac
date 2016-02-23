package com.atlassian.plugin.connect.modules.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Collection;

public class IgnoredEmptyCollectionSerializer implements JsonSerializer<Collection<?>> {

    @Override
    public JsonElement serialize(Collection<?> src, Type typeOfSrc, JsonSerializationContext context) {
        if (null == src || src.isEmpty()) {
            return null;
        }

        JsonArray array = new JsonArray();
        for (Object entry : src) {
            array.add(context.serialize(entry));
        }

        return array;
    }
}
