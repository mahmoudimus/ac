package com.atlassian.plugin.connect.api.util;

import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;

/**
 * A context-specific instance of context parameters for consumption by code calling or preparing a
 * call for remote content.
 */
public class RequestContextParameters
{
    private final Map<String,String> allContextParameters;
    private final Set<String> queryParameters;
    private final Set<String> headerParameters;

    RequestContextParameters(Map<String, String> allContextParameters,
            Set<String> queryParameters, Set<String> headerParameters)
    {
        this.allContextParameters = allContextParameters;
        this.queryParameters = queryParameters;
        this.headerParameters = headerParameters;
    }

    public Map<String, String> getHeaders()
    {
        Map<String,String> headers = newHashMap();

        for (Map.Entry<String,String> entry : allContextParameters.entrySet())
        {
            String name = entry.getKey();
            String value = entry.getValue();
            if (shouldIncludeInHeader(name))
            {
                StringBuilder sb = new StringBuilder("AP-CTX-");
                sb.append(name.replace('_', '-'));

                headers.put(sb.toString(), value);
            }
        }
        return headers;
    }

    public Map<String, String> getQueryParameters()
    {
        Map<String,String> params = newHashMap();

        for (Map.Entry<String,String> entry : allContextParameters.entrySet())
        {
            String name = entry.getKey();
            String value = entry.getValue();
            if (shouldIncludeInQueryString(name))
            {
                params.put("ctx_" + name, value);
            }
        }
        return params;
    }

    private boolean shouldIncludeInQueryString(String key)
    {
        return queryParameters.contains(key);
    }

    private boolean shouldIncludeInHeader(String key)
    {
        return headerParameters.contains(key);
    }
}
