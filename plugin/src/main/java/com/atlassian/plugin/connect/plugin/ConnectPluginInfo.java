package com.atlassian.plugin.connect.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConnectPluginInfo
{
    private static final String BUILD_PROPERTIES = "/build.properties";
    private static final String PLUGIN_VERSION = "plugin.version";
    private static final String BUILD_NUMBER = "build.number";
    private static final String UNFILTERED_VARIABLE = "project.groupId";

    private static String pluginKey;
    private static String pluginVersion;
    private static String buildNumber;

    static
    {
        Properties buildProperties = new Properties();

        try
        {
            InputStream resource = ConnectPluginInfo.class.getResourceAsStream(BUILD_PROPERTIES);
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
            pluginVersion = "0.0";
            buildNumber = "0";
        }
        else
        {
            pluginKey = key;
            pluginVersion = checkNotNull(buildProperties.getProperty(PLUGIN_VERSION), PLUGIN_VERSION);
            buildNumber = checkNotNull(buildProperties.getProperty(BUILD_NUMBER), BUILD_NUMBER);
        }
    }

    public static String getPluginKey()
    {
        return pluginKey;
    }

    public static String getPluginVersion()
    {
        return pluginVersion;
    }

    public static String getBuildNumber()
    {
        return buildNumber;
    }

}
