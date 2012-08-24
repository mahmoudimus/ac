package com.atlassian.labs.remoteapps.host.common.service.http;

import com.atlassian.labs.remoteapps.api.service.http.FormBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Builds HTTP request entity strings for the application/x-www-form-urlencoded content-type.
 */
public class DefaultFormBuilder implements FormBuilder
{
    private Map<String, List<String>> parameters;

    public DefaultFormBuilder()
    {
        parameters = newHashMap();
    }

    public DefaultFormBuilder addParam(String name)
    {
        return addParam(name, null);
    }

    public DefaultFormBuilder addParam(String name, String value)
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

    public DefaultFormBuilder setParam(String name, List<String> values)
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
