package com.atlassian.plugin.remotable.container.internal;

import com.atlassian.plugin.remotable.container.internal.properties.EnvironmentPropertiesLoader;
import com.atlassian.plugin.remotable.container.internal.properties.ResourcePropertiesLoader;
import com.atlassian.plugin.remotable.container.internal.resources.PluginResourceLoader;
import com.atlassian.plugin.remotable.container.internal.resources.ClassLoaderResourceLoader;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableList;
import org.osgi.framework.Bundle;

import java.util.concurrent.ExecutionException;

/**
 * Abstraction for constructing environment properties.  The order goes:
 * <ol>
 * <li>System properties</li>
 * <li>env.properties (loaded from the plugin)</li>
 * <li>{@link com.atlassian.sal.api.pluginsettings.PluginSettings} from SAL</li>
 * </ol>
 */
public final class EnvironmentFactory
{
    private final Cache<BundleKey, Environment> instances;

    public EnvironmentFactory(final PluginSettingsFactory pluginSettingsFactory,
            final PluginAccessor pluginAccessor)
    {
        this.instances = CacheBuilder.newBuilder().weakValues().build(
                new CacheLoader<BundleKey, Environment>()
                {
                    @Override
                    public Environment load(BundleKey key) throws Exception
                    {
                        Plugin plugin = pluginAccessor.getPlugin(key.pluginKey);
                        return new EnvironmentImpl(key.pluginKey,
                                pluginSettingsFactory,
                                ImmutableList.of(
                                        new ResourcePropertiesLoader("/env-defaults.properties", new ClassLoaderResourceLoader(this.getClass())),
                                        new ResourcePropertiesLoader("/env.properties", new PluginResourceLoader(plugin)),
                                        new ResourcePropertiesLoader("/env.local.properties", new PluginResourceLoader(plugin)),
                                        new EnvironmentPropertiesLoader())
                        );
                    }
                });
    }

    public Environment getService(Bundle bundle)
    {
        try
        {
            return instances.get(new BundleKey(bundle));
        }
        catch (ExecutionException e)
        {
            throw new IllegalStateException(e);
        }
    }
}
