package com.atlassian.labs.remoteapps.apputils.spring;

import com.atlassian.labs.remoteapps.apputils.Environment;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

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
 *     <li>{@link com.atlassian.sal.api.pluginsettings.PluginSettings} from SAL</li>
 * </ol>
 *
 * Any updates to the properties will save to plugin settings.  These keys will be prefixed
 * by "APP_KEY." to avoid collisions.
 */
public class EnvironmentImpl implements Environment
{

    private final Properties ENV;
    private final String appKey;
    private final PluginSettingsFactory pluginSettingsFactory;

    public EnvironmentImpl(String appKey, PluginSettingsFactory pluginSettingsFactory)
    {
        this.appKey = appKey;
        this.pluginSettingsFactory = pluginSettingsFactory;
        ENV = new Properties();
        try
        {
            ENV.load(EnvironmentImpl.class.getResourceAsStream("/env-defaults.properties"));
            InputStream env = EnvironmentImpl.class.getResourceAsStream("/env.properties");
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

    /**
     * @return All clients defined in environment variables or env.properties.  Plugin settings
     * will be skipped.
     */
    public Iterable<String> getAllClients()
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

    @Override
    public String getEnv(String name)
    {
        String val = getOptionalEnv(name, null);
        if (val == null)
        {
            throw new IllegalArgumentException("Missing environment variable: " + name);
        }
        else
        {
            return val;
        }
    }

    @Override
    public String getOptionalEnv(String name, String def)
    {
        String val = ENV.getProperty(name);
        if (val == null)
        {
            PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            val = (String) settings.get(appKey + "." + name);
        }
        if (val == null)
        {
            return def;
        }
        return val.replaceAll("\\\\n", "\n");
    }

    @Override
    public void setEnv(String name, String value)
    {
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        settings.put(appKey + "." + name, value);
    }

    @Override
    public void setEnvIfNull(String name, String value)
    {
        if (getOptionalEnv(name, null) == null)
        {
            setEnv(name, value);
        }
    }
}
