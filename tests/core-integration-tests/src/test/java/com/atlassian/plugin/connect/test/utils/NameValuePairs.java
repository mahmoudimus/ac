package com.atlassian.plugin.connect.test.utils;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public class NameValuePairs
{
    public static final NameValuePair EMPTY_PAIR = new BasicNameValuePair("", "");

    private final List<NameValuePair> nameValuePairs;

    public NameValuePairs(String queryString)
    {
        this.nameValuePairs = URLEncodedUtils.parse(queryString, Charset.forName("UTF-8"));
    }

    public NameValuePairs(Map<String, String[]> parameters)
    {
        this.nameValuePairs = Lists.newArrayList();
        for (Map.Entry<String, String[]> entry : parameters.entrySet())
        {
            for (String value : entry.getValue())
            {
                nameValuePairs.add(new BasicNameValuePair(entry.getKey(), value));
            }
        }
    }

    public List<NameValuePair> all(final String name)
    {
        return ImmutableList.copyOf(Iterables.filter(nameValuePairs, new Predicate<NameValuePair>()
        {
            @Override
            public boolean apply(NameValuePair input)
            {
                return name.equals(input.getName());
            }
        }));
    }

    public List<NameValuePair> allStartingWith(final String prefix)
    {
        return ImmutableList.copyOf(Iterables.filter(nameValuePairs, new Predicate<NameValuePair>()
        {
            @Override
            public boolean apply(NameValuePair input)
            {
                return input.getName().startsWith(prefix);
            }
        }));
    }

    public NameValuePair any(final String name)
    {
        return any(name, EMPTY_PAIR);
    }

    public NameValuePair any(final String name, NameValuePair defaultIfNotFound)
    {
        return Iterables.find(nameValuePairs, new Predicate<NameValuePair>()
        {
            @Override
            public boolean apply(NameValuePair input)
            {
                return name.equals(input.getName());
            }
        }, defaultIfNotFound);
    }

    public List<NameValuePair> getNameValuePairs()
    {
        return nameValuePairs;
    }
}
