package com.atlassian.plugin.connect.util.fixture;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;

/**
 * @since 1.0
 */
public class RemotablePluginAccessorFactoryForTests implements RemotablePluginAccessorFactory
{
    public static final String ADDON_BASE_URL = "http://www.example.com";
    private String baseUrl;

    public RemotablePluginAccessorFactoryForTests()
    {
        baseUrl = ADDON_BASE_URL;
    }

    public void withBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    @Override
    public RemotablePluginAccessor getOrThrow(final String pluginKey)
    {
        return get(pluginKey); // it's never null
    }

    @Override
    public void remove(String pluginKey)
    {
        //do nothing
    }

    @Override
    public RemotablePluginAccessor get(final String pluginKey)
    {
        return new RemotablePluginAccessorForTests(pluginKey, pluginKey, baseUrl);
    }

    @Override
    public RemotablePluginAccessor get(Plugin plugin)
    {
        return get(plugin.getKey());
    }

    @Override
    public RemotablePluginAccessor get(ConnectAddonBean addon)
    {
        return get(addon.getKey());
    }
}
