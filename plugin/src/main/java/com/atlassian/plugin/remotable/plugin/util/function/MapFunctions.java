package com.atlassian.plugin.remotable.plugin.util.function;

import com.google.common.base.Function;

import javax.annotation.Nullable;
import java.util.List;

import static java.util.Collections.singletonList;

public final class MapFunctions
{
    private MapFunctions()
    {
    }

    public static final Function<Object, String> OBJECT_TO_STRING = new Function<Object, String>()
    {
        @Override
        public String apply(Object from)
        {
            return from != null ? from.toString() : null;
        }
    };

    public static final Function<String[], String> STRING_ARRAY_TO_STRING = new Function<String[], String>()
    {
        @Override
        public String apply(String[] from)
        {
            return from != null && from.length > 0 ? from[0] : null;
        }
    };

    public static final Function<String, List<String>> STRING_TO_LIST = new Function<String, List<String>>()
    {
        @Override
        public List<String> apply(@Nullable String s)
        {
            return singletonList(s);
        }
    };
}
