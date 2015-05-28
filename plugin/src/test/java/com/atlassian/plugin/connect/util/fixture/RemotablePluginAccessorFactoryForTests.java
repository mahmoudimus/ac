package com.atlassian.plugin.connect.util.fixture;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.DefaultRemotablePluginAccessorBase;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.http.AuthorizationGenerator;
import com.atlassian.plugin.connect.api.http.HttpMethod;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Supplier;

import java.net.URI;
import java.util.Map;

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
        Supplier<URI> supplier = new Supplier<URI>()
        {
            @Override
            public URI get()
            {
                return URI.create(baseUrl);
            }
        };
        return new DefaultRemotablePluginAccessorBase(pluginKey, pluginKey, supplier, null)
        {
            @Override
            public String signGetUrl(URI targetPath, Map<String, String[]> params)
            {
                return "";
            }

            @Override
            public Promise<String> executeAsync(HttpMethod method, URI path, Map<String, String[]> params, Map<String, String> headers)
            {
                return null;
            }

            @Override
            public AuthorizationGenerator getAuthorizationGenerator()
            {
                return null;
            }
        };
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
