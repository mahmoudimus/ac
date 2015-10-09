package com.atlassian.plugin.connect.api.util;

import com.atlassian.uri.UriBuilder;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class UriBuilderUtils
{
    public static void addQueryParameters(UriBuilder uriBuilder, Map<String, String[]> parameters)
    {
        for (Map.Entry<String, String[]> entry : parameters.entrySet())
        {
            String[] values = entry.getValue();
            if (null == values || values.length == 0)
            {
                uriBuilder.addQueryParameter(entry.getKey(), "");
            }
            else
            {
                for (String value : values)
                {
                    uriBuilder.addQueryParameter(entry.getKey(), value);
                }
            }
        }
    }

    public static Map<String, List<String>> toListFormat(Map<String, String[]> allParameters)
    {
        return Maps.transformValues(allParameters, new Function<String[], List<String>>()
        {
            @Override
            public List<String> apply(@Nullable String[] input)
            {
                return null == input ? Lists.<String>newArrayList() : Lists.newArrayList(input);
            }
        });
    }
}
