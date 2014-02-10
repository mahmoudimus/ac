package com.atlassian.plugin.connect.plugin.util;

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
            for (String value : entry.getValue())
            {
                uriBuilder.addQueryParameter(entry.getKey(), value);
            }
        }
    }

    public static Map<String, String[]> toMultiValue(Map<String, String> urlParameters)
    {
        return Maps.transformValues(urlParameters, new Function<String, String[]>()
        {
            @Override
            public String[] apply(@Nullable String input)
            {
                return null == input ? new String[0] : new String[]{input};
            }
        });
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
