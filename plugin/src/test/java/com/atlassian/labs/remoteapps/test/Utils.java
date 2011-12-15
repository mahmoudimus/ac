package com.atlassian.labs.remoteapps.test;

import com.atlassian.pageobjects.ProductInstance;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

/**
 *
 */
public class Utils
{
    public static String loadResourceAsString(String path) throws IOException
    {
        return IOUtils.toString(Utils.class.getClassLoader().getResourceAsStream(path));
    }

    public static String getJson(String url) throws IOException
    {
        InputStream in = new URL(url).openStream();
        StringWriter writer = new StringWriter();
        IOUtils.copy(in, writer);
        in.close();
        return writer.toString();
    }


}
