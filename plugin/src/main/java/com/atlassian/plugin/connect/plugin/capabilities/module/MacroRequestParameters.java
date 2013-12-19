package com.atlassian.plugin.connect.plugin.capabilities.module;

import com.atlassian.sal.api.user.UserProfile;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class MacroRequestParameters
{
    public static class Builder
    {
        private final Map<String, List<String>> queryParameters;

        public Builder()
        {
            queryParameters = Maps.newHashMap();
        }

        public Builder withSingleValueParameters(Map<String, String> map)
        {
            for (Map.Entry<String, String> entry : map.entrySet())
            {
                withParameter(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public Builder withMultiValueParameters(Map<String, List<String>> map)
        {
            for (Map.Entry<String, List<String>> entry : map.entrySet())
            {
                List<String> list = getList(entry.getKey());
                list.addAll(entry.getValue());
            }
            return this;
        }

        public Builder withBody(String value)
        {
            return withParameter("body", value);
        }

        public Builder withUser(UserProfile user)
        {
            return withParameter("user_key", null == user ? "" : user.getUserKey().getStringValue())
                   .withParameter("user_id", null == user ? "" : user.getUsername());
        }

        private Builder withParameter(String key, String value)
        {
            List<String> list = getList(key);
            list.add(value);
            return this;
        }

        private List<String> getList(String key)
        {
            List<String> list = queryParameters.get(key);
            if (null == list)
            {
                list = Lists.newArrayList();
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
                return from.get(0);
            }
        });
    }
}
