package com.atlassian.labs.remoteapps.host.common.service;

import com.atlassian.labs.remoteapps.api.service.PluginSettingsAsync;
import com.atlassian.labs.remoteapps.api.service.PluginSettingsAsyncFactory;
import com.atlassian.labs.remoteapps.spi.util.ServiceWrappers;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

/**
 * Simple factory that creates the right plugin settings instances
 */
public class DefaultPluginSettingsAsyncFactory implements PluginSettingsAsyncFactory
{
    private final PluginSettingsFactory delegate;

    public DefaultPluginSettingsAsyncFactory(PluginSettingsFactory delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public PluginSettingsAsync createSettingsForKey(String key)
    {
        return ServiceWrappers.wrapAsAsync(delegate.createSettingsForKey(key),
                PluginSettingsAsync.class);
    }

    @Override
    public PluginSettingsAsync createGlobalSettings()
    {
        return ServiceWrappers.wrapAsAsync(delegate.createGlobalSettings(),
                PluginSettingsAsync.class);
    }
}
