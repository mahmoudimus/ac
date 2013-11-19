package com.atlassian.json.schema.util;

public class StringUtil
{
    public static boolean isBlank(String s)
    {
        return (null == s || s.length() < 1);
    }

    public static boolean isNotBlank(String s)
    {
        return !isBlank(s);
    }
    
    public static String lowerCamel(String s)
    {
        if(s.length() < 1)
        {
            return s;
        }
        
        if(s.length() < 2)
        {
            return s.toLowerCase();
        }
        
        return s.substring(0,1).toLowerCase() + s.substring(1,s.length());
    }
}
