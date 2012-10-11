package com.atlassian.plugin.remotable.container.internal;

import com.atlassian.plugin.remotable.container.internal.properties.PropertiesLoader;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.*;

/**
 * Abstraction for retrieving environment properties.  The order is decided by the properties loaders passed to the constructor.
 *
 * Any updates to the properties will save to plugin settings.  These keys will be prefixed
 * by "APP_KEY." to avoid collisions.
 */
public final class EnvironmentImpl implements Environment
{
    private final String appKey;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final Map<String, String> env;

    public EnvironmentImpl(String appKey, PluginSettingsFactory pluginSettingsFactory, Iterable<PropertiesLoader> propertiesLoaders)
    {
        this.appKey = checkNotNull(appKey);
        this.pluginSettingsFactory = checkNotNull(pluginSettingsFactory);
        this.env = loadEnv(propertiesLoaders);
    }

    private ImmutableMap<String, String> loadEnv(Iterable<PropertiesLoader> propertiesLoaders)
    {
        final Map<String, String> envBuilder = newHashMap();
        for (PropertiesLoader properties : propertiesLoaders)
        {
            envBuilder.putAll(properties.load());
        }
        return ImmutableMap.copyOf(envBuilder);
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
        String val = env.get(name);
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
