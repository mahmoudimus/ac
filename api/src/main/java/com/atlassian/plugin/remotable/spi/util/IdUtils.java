package com.atlassian.plugin.remotable.spi.util;

import com.google.common.base.Function;

/**
 */
public final class IdUtils
{
    private static final Function<Character, String> CAMEL_CASER = new Function<Character, String>()
    {
        @Override
        public String apply(Character input)
        {
            return String.valueOf(Character.toUpperCase(input));
        }
    };

    private static final Function<Character, String> TITLER = new Function<Character, String>()
    {
        @Override
        public String apply(Character input)
        {
            return " " + Character.toUpperCase(input);
        }
    };

    public static String dashesToCamelCase(String id)
    {
        return process(id, CAMEL_CASER);
    }

    public static String dashesToTitle(String id)
    {
        return process(id, TITLER).substring(1);
    }

    private static String process(String id, Function<Character,String> converter)
    {
        StringBuilder sb = new StringBuilder();

        char prev = '-';
        for (int x=0; x < id.length(); x++)
        {
            char cur = id.charAt(x);
            if (prev == '-')
            {
                sb.append(converter.apply(cur));
            }
            else if (cur != '-')
            {
                sb.append(cur);
            }
            prev = cur;
        }
        return sb.toString();
    }

}
