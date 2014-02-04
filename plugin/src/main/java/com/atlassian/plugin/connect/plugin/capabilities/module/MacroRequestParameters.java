package com.atlassian.plugin.connect.plugin.capabilities.module;

import java.util.List;
import java.util.Map;

import com.atlassian.renderer.v2.macro.Macro;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public class MacroRequestParameters
{
    public static class Builder
    {
        private final Map<String, List<String>> queryParameters;

        public Builder()
        {
            queryParameters = newHashMap();
        }

        public Builder withMacroParameters(Map<String, String> map)
        {
            for (Map.Entry<String, String> entry : map.entrySet())
            {
                withParameter(entry.getKey(), entry.getValue());
            }
            queryParameters.remove(Macro.RAW_PARAMS_KEY);
            return this;
        }

        public Builder withURLParameters(Map<String, List<String>> map)
        {
            for (Map.Entry<String, List<String>> entry : map.entrySet())
            {
                List<String> list = getorCreateList(entry.getKey());
                list.addAll(entry.getValue());
            }
            return this;
        }

        private Builder withParameter(String key, String value)
        {
            List<String> list = getorCreateList(key);
            list.add(value);
            return this;
        }

        private List<String> getorCreateList(String key)
        {
            List<String> list = queryParameters.get(key);
            if (null == list)
            {
                list = newArrayList();
                queryParameters.put(key, list);
            }
            return list;
        }

        public Map<String, List<String>> getQueryParameters()
        {
            return queryParameters;
        }

        public MacroRequestParameters build()
        {
            return new MacroRequestParameters(this);
        }
    }

    private Map<String, List<String>> parameters;

    private MacroRequestParameters(Builder builder)
    {
        this.parameters = builder.getQueryParameters();
    }

    public Map<String, String[]> getQueryParameters()
    {
        return Maps.transformValues(parameters, new Function<List<String>, String[]>()
        {
            @Override
            public String[] apply(List<String> from)
            {
                return from.toArray(new String[from.size()]);
            }
        });
    }

    public Map<String, String> getSingleQueryParameters()
    {
        return Maps.transformValues(parameters, new Function<List<String>, String>()
        {
            @Override
            public String apply(List<String> from)
            {
                Preconditions.checkArgument(from.size() == 1, "Multi-valued query parameters are not supported yet: {}", from);
                return from.get(0);
            }
        });
    }
}
