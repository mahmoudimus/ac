package com.atlassian.json.schema.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

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

    public static String indent(String input, int indent)
    {
        StringBuilder sb = new StringBuilder();

        BufferedReader bufReader = new BufferedReader(new StringReader(input));

        String line=null;
        String indented = input;
        String spaces = StringUtil.repeat(' ', indent);
        try
        {
            while((line=bufReader.readLine()) != null)
            {
                sb.append(spaces).append(line).append(System.getProperty("line.separator"));
            }
            
            indented = sb.toString();
        }
        catch (IOException e)
        {
            //ignore, the input will be returned if there's an error
        }

        return indented;
    }

    private static String repeat(char c, int num)
    {
        StringBuilder sb = new StringBuilder();
        
        for(int i=0; i<num;i++)
        {
            sb.append(c);
        }
        
        return sb.toString();
    }


}
