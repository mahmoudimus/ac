package com.atlassian.plugin.connect.util.fixture;

import com.atlassian.plugin.connect.api.request.RemotablePluginAccessor;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessorFactory;

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
    public void remove(String pluginKey)
    {
        //do nothing
    }

    @Override
    public RemotablePluginAccessor get(final String pluginKey)
    {
        return new RemotablePluginAccessorForTests(pluginKey, pluginKey, baseUrl);
    }
}
