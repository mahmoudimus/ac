package com.atlassian.labs.remoteapps.util.uri;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import static com.atlassian.labs.remoteapps.util.uri.Pair.pair;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;

public final class Uris
{
    private Uris()
    {
        throw new RuntimeException("UriEncoder cannot be instantiated");
    }

    public static Function<String, String> encode()
    {
        return Encode.INSTANCE;
    }

    private enum Encode implements Function<String, String>
    {
        INSTANCE;

        public String apply(String s)
        {
            try
            {
                return URLEncoder.encode(s, "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException("Funny JVM you have here", e);
            }
        }
    }

    public static String encode(final String uriComponent)
    {
        return encode().apply(uriComponent);
    }

    public static String encode(final String uriComponent, String encoding)
    {
        try
        {
            return URLEncoder.encode(uriComponent, encoding);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String decode(String uriComponent)
    {
        try
        {
            return URLDecoder.decode(uriComponent, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("Funny JVM you have here", e);
        }
    }

    public static String decode(String uriComponent, String encoding)
    {
        try
        {
            return URLDecoder.decode(uriComponent, encoding);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static Multimap<String, String> getQueryParameters(URI uri)
    {
        if (uri.getQuery() == null)
        {
            return ImmutableMultimap.of();
        }
        ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
        for (Pair<String, String> param : transform(asList(uri.getQuery().split("&")), asQueryParam()))
        {
            builder.put(param.first(), param.second());
        }
        return builder.build();
    }

    private static Function<String, Pair<String, String>> asQueryParam()
    {
        return AsQueryParam.INSTANCE;
    }

    private enum AsQueryParam implements Function<String, Pair<String, String>>
    {
        INSTANCE;

        public Pair<String, String> apply(String p)
        {
            String[] nameValue = p.split("=");
            String name = decode(nameValue[0]);
            String value = nameValue.length == 2 ? decode(nameValue[1]) : "";
            return pair(name, value);
        }
    }
}
