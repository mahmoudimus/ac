package com.atlassian.labs.remoteapps.container.internal;

import com.atlassian.labs.remoteapps.container.internal.properties.EnvironmentPropertiesLoader;
import com.atlassian.labs.remoteapps.container.internal.properties.PropertiesLoader;
import com.atlassian.labs.remoteapps.container.internal.properties.ResourcePropertiesLoader;
import com.atlassian.labs.remoteapps.container.internal.resources.ClassLoaderResourceLoader;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.google.common.base.Preconditions.*;

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
        final ImmutableMap.Builder<String, String> envBuilder = ImmutableMap.builder();
        for (PropertiesLoader properties : propertiesLoaders)
        {
            envBuilder.putAll(properties.load());
        }
        return envBuilder.build();
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
