package com.atlassian.labs.remoteapps.api.service.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Builds HTTP request entity strings for the application/x-www-form-urlencoded content-type.
 */
public class FormBuilder
{
    public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";

    private Map<String, List<String>> parameters;

    private FormBuilder()
    {
        parameters = newHashMap();
    }

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

    public String toEntity()
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

    public String toString()
    {
        return toEntity();
    }

    public static FormBuilder create()
    {
        return new FormBuilder();
    }

    public static FormBuilder fromSingleValuedMap(Map<String, String> parameters)
    {
        FormBuilder builder = new FormBuilder();
        for (Map.Entry<String, String> entry : parameters.entrySet())
        {
            String name = entry.getKey();
            String value = entry.getValue();
            builder.addParam(name, value);
        }
        return builder;
    }

    public static FormBuilder fromMultiValuedMap(Map<String, List<String>> parameters)
    {
        FormBuilder builder = new FormBuilder();
        for (Map.Entry<String, List<String>> entry : parameters.entrySet())
        {
            String name = entry.getKey();
            List<String> values = entry.getValue();
            builder.setParam(name, values);
        }
        return builder;
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
