package com.atlassian.plugin.connect.modules.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LowercaseEnumTypeAdapterFactory implements TypeAdapterFactory
{
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type)
    {
        Class<T> rawType = (Class<T>) type.getRawType();
        if (!rawType.isEnum())
        {
            return null;
        }

        final Map<String, T> lowercaseToConstant = new HashMap<String, T>();
        for (T constant : rawType.getEnumConstants())
        {
            lowercaseToConstant.put(toLowercase(constant), constant);
        }

        return new LowercaseEnumTypeAdapter(lowercaseToConstant).nullSafe();
    }

    private String toLowercase(Object o)
    {
        return o.toString().toLowerCase();
    }

    private class LowercaseEnumTypeAdapter<T> extends TypeAdapter<T>
    {
        private Map<String, T> lowercaseToConstant;

        public LowercaseEnumTypeAdapter(Map<String, T> lowercaseToConstant)
        {
            this.lowercaseToConstant = lowercaseToConstant;
        }

        public void write(JsonWriter out, T value) throws IOException
        {
            if (null == value)
            {
                out.nullValue();
            }
            else
            {
                out.value(toLowercase(value));
            }
        }

        public T read(JsonReader reader) throws IOException
        {
            return lowercaseToConstant.get(reader.nextString().toLowerCase());
        }
    }
}
