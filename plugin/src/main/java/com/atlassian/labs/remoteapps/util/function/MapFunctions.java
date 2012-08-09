package com.atlassian.labs.remoteapps.util.function;

import com.google.common.base.Function;

/**
 */
public class MapFunctions
{
    public static final Function<Object,String> OBJECT_TO_STRING = new Function<Object, String>()
    {
        @Override
        public String apply(Object from)
        {
            return from != null ? from.toString() : null;
        }
    };

    public static final Function<String[],String> STRING_ARRAY_TO_STRING = new Function<String[], String>()
    {
        @Override
        public String apply(String[] from)
        {
            return from != null && from.length > 0 ? from[0] : null;
        }
    };
}
