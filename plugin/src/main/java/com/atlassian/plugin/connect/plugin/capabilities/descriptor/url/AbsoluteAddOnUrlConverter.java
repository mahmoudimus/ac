package com.atlassian.plugin.connect.plugin.capabilities.descriptor.url;

import java.net.URI;
import java.net.URISyntaxException;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.uri.UriBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Converts URLs specified by Add-On descriptors into absolute URLs.
 * Either by resolving them against the Add-On base url, or by just returning them if they
 * were already absolute.
 */
@Component
public class AbsoluteAddOnUrlConverter
{
    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;

    @Autowired
    public AbsoluteAddOnUrlConverter(RemotablePluginAccessorFactory remotablePluginAccessorFactory)
    {
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
    }

    public String getAbsoluteUrl(String pluginKey, String url) throws URISyntaxException
    {
        URI uri = new URI(url);
        if (!uri.isAbsolute())
        {
            RemotablePluginAccessor remotablePluginAccessor = remotablePluginAccessorFactory.get(pluginKey);
            URI baseUrl = remotablePluginAccessor.getBaseUrl();
            return new UriBuilder()
                    .setScheme(baseUrl.getScheme())
                    .setAuthority(baseUrl.getAuthority())
                    .setPath(uri.getPath())
                    .setQuery(uri.getQuery())
                    .setFragment(uri.getFragment())
                    .toString();
        }
        return url;
    }

    public String getAbsoluteUrl(ConnectAddonBean addon, String url)
    {
        try
        {
            return getAbsoluteUrl(addon.getKey(), url);
        }
        catch (URISyntaxException e)
        {
            throw new PluginParseException("Malformed url declared by '"
                    + addon.getName()
                    + "' (" + addon.getKey() + "): "
                    + url, e);
        }
    }
}
