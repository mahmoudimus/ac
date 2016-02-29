package com.atlassian.plugin.connect.test.common.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NameValuePairs {
    public static final NameValuePair EMPTY_PAIR = new BasicNameValuePair("", "");

    private final List<NameValuePair> nameValuePairs;

    public NameValuePairs(String queryString) {
        this.nameValuePairs = URLEncodedUtils.parse(queryString, Charset.forName("UTF-8"));
    }

    public NameValuePairs(Map<String, String[]> parameters) {
        this.nameValuePairs = Lists.newArrayList();
        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            for (String value : entry.getValue()) {
                nameValuePairs.add(new BasicNameValuePair(entry.getKey(), value));
            }
        }
    }

    public List<NameValuePair> all(final String name) {
        return ImmutableList.copyOf(nameValuePairs.stream().filter(input -> name.equals(input.getName())).collect(
                Collectors.toList()));
    }

    public List<NameValuePair> allStartingWith(final String prefix) {
        return ImmutableList.copyOf(nameValuePairs.stream().filter(input -> input.getName().startsWith(prefix)).collect(
                Collectors.toList()));
    }

    public NameValuePair any(final String name) {
        return any(name, EMPTY_PAIR);
    }

    public NameValuePair any(final String name, NameValuePair defaultIfNotFound) {
        return Iterables.find(nameValuePairs, input -> name.equals(input.getName()), defaultIfNotFound);
    }

    public List<NameValuePair> getNameValuePairs() {
        return nameValuePairs;
    }
}
