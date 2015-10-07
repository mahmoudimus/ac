package com.atlassian.plugin.connect.util;

import com.atlassian.plugin.connect.api.util.ConnectPluginInfo;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class ConnectPluginInfoTest
{
    private static final String BUILD_PROPERTIES = "/build.properties";
    private static final String UNFILTERED_VARIABLE = "project.groupId";

    private static String pluginKey;

    @Before
    public void setUp() throws Exception
    {
        Properties buildProperties = new Properties();

        try
        {
            InputStream resource = ConnectPluginInfoTest.class.getResourceAsStream(BUILD_PROPERTIES);
            if (resource == null) {
                throw new IllegalStateException("Couldn't load " + BUILD_PROPERTIES + " from classpath");
            }
            buildProperties.load(resource);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Failed to read from " + BUILD_PROPERTIES + "!", e);
        }

        String key = buildProperties.getProperty("plugin.key");
        if (key == null || key.contains(UNFILTERED_VARIABLE))
        {
            // needed for local testing
            pluginKey = "com.atlassian.plugins.atlassian-connect-plugin";
        }
        else
        {
            pluginKey = key;
        }
    }

    @Test
    public void testPluginKeyNotChanged() throws Exception
    {
        assertEquals(ConnectPluginInfo.getPluginKey(), pluginKey);
    }
}
