package com.atlassian.labs.remoteapps.kit.js;

import com.samskivert.mustache.Mustache;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * Used by atlassian/mustache
 */
public class HttpUtils
{
    public static String render(String template, Map<String,Object> context)
    {
        StringWriter writer = new StringWriter();
        InputStream resourceAsStream = null;
        try
        {
            resourceAsStream = HttpUtils.class.getClassLoader().getResourceAsStream(template);
            Mustache.compiler().compile(
                new InputStreamReader(resourceAsStream)).execute(context,
                writer);
        }
        finally
        {
            try
            {
                resourceAsStream.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        return writer.toString();
    }
}
