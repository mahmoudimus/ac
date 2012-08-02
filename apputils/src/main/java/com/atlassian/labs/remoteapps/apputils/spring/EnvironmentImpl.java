package com.atlassian.labs.remoteapps.apputils.spring;

import com.atlassian.labs.remoteapps.apputils.Environment;
import com.atlassian.labs.remoteapps.apputils.spring.properties.EnvironmentPropertiesLoader;
import com.atlassian.labs.remoteapps.apputils.spring.properties.ResourcePropertiesLoader;
import com.atlassian.labs.remoteapps.apputils.spring.resources.ClassLoaderResourceLoader;
import com.atlassian.labs.remoteapps.apputils.spring.resources.ResourceLoader;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstraction for retrieving environment properties.  The order goes:
 * <ol>
 * <li>System properties</li>
 * <li>env.properties (loaded from the classpath)</li>
 * <li>{@link com.atlassian.sal.api.pluginsettings.PluginSettings} from SAL</li>
 * </ol>
 *
 * Any updates to the properties will save to plugin settings.  These keys will be prefixed
 * by "APP_KEY." to avoid collisions.
 */
public final class EnvironmentImpl implements Environment
{
    private final String appKey;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final ResourceLoader resourceLoader;
    private final Map<String, String> env;

    public EnvironmentImpl(String appKey, PluginSettingsFactory pluginSettingsFactory)
    {
        this(appKey, pluginSettingsFactory, new ClassLoaderResourceLoader(EnvironmentImpl.class));
    }

    public EnvironmentImpl(String appKey, PluginSettingsFactory pluginSettingsFactory, ResourceLoader resourceLoader)
    {
        this.appKey = checkNotNull(appKey);
        this.pluginSettingsFactory = checkNotNull(pluginSettingsFactory);
        this.resourceLoader = checkNotNull(resourceLoader);
        this.env = loadEnv();
    }

    private ImmutableMap<String, String> loadEnv()
    {
        final ImmutableMap.Builder<String, String> envBuilder = ImmutableMap.builder();
        envBuilder.putAll(new ResourcePropertiesLoader("/env-defaults.properties", resourceLoader).load());
        envBuilder.putAll(new ResourcePropertiesLoader("/env.properties", resourceLoader).load());
        envBuilder.putAll(new EnvironmentPropertiesLoader().load());
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
