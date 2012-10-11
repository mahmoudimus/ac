package com.atlassian.plugin.remotable.spi.http;

import com.atlassian.plugin.remotable.api.service.http.FormBuilder;
import com.google.common.collect.ImmutableMap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newLinkedHashMap;

public class DefaultFormBuilder implements FormBuilder
{
    private Map<String, List<String>> parameters = newLinkedHashMap();

    public FormBuilder addParam(String name)
    {
        return addParam(name, null);
    }

    public FormBuilder addParam(String name, String value)
    {
        List<String> values = parameters.get(name);
        if (values == null)
        {
            values = newLinkedList();
            parameters.put(name, values);
        }
        values.add(value);
        return this;
    }

    public FormBuilder setParam(String name, List<String> values)
    {
        parameters.put(name, newLinkedList(values));
        return this;
    }

    public Entity build()
    {
        StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, List<String>> entry : parameters.entrySet())
        {
            String name = encode(entry.getKey());
            List<String> values = entry.getValue();
            for (String value : values)
            {
                if (first)
                {
                    first = false;
                }
                else
                {
                    buf.append("&");
                }
                buf.append(name);
                if (value != null)
                {
                    buf.append("=");
                    buf.append(encode(value));
                }
            }
        }

        final byte[] bytes = buf.toString().getBytes(Charset.forName("UTF-8"));

        return new Entity()
        {
            @Override
            public Map<String, String> getHeaders()
            {
                return ImmutableMap.of("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            }

            @Override
            public InputStream getInputStream()
            {
                return new ByteArrayInputStream(bytes);
            }

            @Override
            public String toString()
            {
                return new String(bytes, Charset.forName("UTF-8"));
            }
        };
    }

    private String encode(String str)
    {
        try
        {
            str = URLEncoder.encode(str, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
        return str;
    }
}
