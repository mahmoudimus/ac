package com.atlassian.labs.remoteapps.spi.http;

import com.atlassian.labs.remoteapps.api.service.http.FormBuilder;
import com.google.common.collect.Maps;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;

public class DefaultFormBuilder implements FormBuilder
{
    private Map<String, List<String>> parameters = newHashMap();

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

    @Override
    public Map<String, String> getHeaders()
    {
        Map<String, String> headers = Maps.newHashMap();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        return headers;
    }

    @Override
    public InputStream build()
    {
        try
        {
            return new ByteArrayInputStream(toString().getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString()
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
        return buf.toString();
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
