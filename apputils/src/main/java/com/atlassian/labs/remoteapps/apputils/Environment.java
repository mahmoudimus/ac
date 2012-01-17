package com.atlassian.labs.remoteapps.apputils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Abstraction for retrieving environment properties.  The order goes:
 * <ol>
 *     <li>System properties</li>
 *     <li>env.properties (loaded from the classpath)</li>
 *     <li>env-defaults.properties (built-in)</li>
 * </ol>
 */
public class Environment
{

    // necessary for testing
    private static final Properties ENV;

    static
    {
        ENV = new Properties();
        try
        {
            ENV.load(Environment.class.getResourceAsStream("/env-defaults.properties"));
            InputStream env = Environment.class.getResourceAsStream("/env.properties");
            if (env != null)
            {
                ENV.load(env);
            }
            ENV.putAll(System.getenv());

        }
        catch (IOException e)
        {
            throw new RuntimeException("Couldn't load env properties");
        }
    }

    public static int getEnvAsInt(String name)
    {
        return Integer.parseInt(getEnv(name));
    }

    public static Iterable<String> getAllClients()
    {
        Set<String> keys = new HashSet<String>();
        for (String key : ENV.stringPropertyNames())
        {
            if (key.startsWith("HOST_BASE_URL."))
            {
                keys.add(key.substring("HOST_BASE_URL.".length()));
            }
        }
        return keys;
    }

    public static String getEnv(String name)
    {
        String val = ENV.getProperty(name);
        if (val == null)
        {
            throw new IllegalArgumentException("Missing environment variable: " + name);
        }
        return val.replaceAll("\\\\n", "\n");
    }

    // should only be used for testing
    public static void setEnv(String name, String value)
    {
        ENV.setProperty(name, value);
    }
}
