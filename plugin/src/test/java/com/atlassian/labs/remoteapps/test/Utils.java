package com.atlassian.labs.remoteapps.test;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 *
 */
public class Utils
{
    public static String loadResourceAsString(String path) throws IOException
    {
        return IOUtils.toString(Utils.class.getClassLoader().getResourceAsStream(path));
    }
}
