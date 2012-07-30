package com.atlassian.labs.remoteapps.container.internal;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA. User: mrdon Date: 7/27/12 Time: 2:22 PM To change this template use
 * File | Settings | File Templates.
 */
public class EnvironmentImplFactory
{
    private final Cache<String, Environment> instances;

    public EnvironmentImplFactory(final PluginSettingsFactory pluginSettingsFactory)
    {
        this.instances = CacheBuilder.newBuilder().weakValues().build(
                new CacheLoader<String, Environment>()
                {
                    @Override
                    public Environment load(String key) throws Exception
                    {
                        return new EnvironmentImpl(key, pluginSettingsFactory);
                    }
                });
    }

    public Environment getService(String appKey)
    {
        try
        {
            return instances.get(appKey);
        }
        catch (ExecutionException e)
        {
            throw new IllegalStateException(e);
        }
    }
}
