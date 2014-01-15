package com.atlassian.plugin.connect.modules.gson;

import com.google.common.base.Strings;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class EmptyStringIgnoringTypeAdapter extends TypeAdapter<String>
{

    @Override
    public void write(JsonWriter out, String value) throws IOException
    {
        if (!Strings.isNullOrEmpty(value))
        {
            out.value(value);
        }
        else
        {
            out.nullValue();
        }
    }

    @Override
    public String read(JsonReader in) throws IOException
    {
        String result = in.nextString();
        if (!Strings.isNullOrEmpty(result))
        {
            return result;
        }

        return null;
    }
}
