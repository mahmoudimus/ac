package com.atlassian.plugin.connect.plugin;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.util.http.HttpContentRetriever;
import com.atlassian.plugin.connect.spi.http.AuthorizationGenerator;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.google.common.base.Supplier;

import java.net.URI;
import java.util.Map;

public class NoAuthRemotablePluginAccessor extends DefaultRemotablePluginAccessorBase
{
    public NoAuthRemotablePluginAccessor(Plugin plugin, Supplier<URI> baseUrl, HttpContentRetriever httpContentRetriever)
    {
        super(plugin, baseUrl, httpContentRetriever);
    }

    public NoAuthRemotablePluginAccessor(ConnectAddonBean addon, Supplier<URI> baseUrl, HttpContentRetriever httpContentRetriever)
    {
        super(addon.getKey(), addon.getName(), baseUrl, httpContentRetriever);
    }

    @Override
    public String signGetUrl(URI targetPath, Map<String, String[]> params)
    {
        return createGetUrl(targetPath, params);
    }

    @Override
    public AuthorizationGenerator getAuthorizationGenerator()
    {
        return new AuthorizationGenerator()
        {
            @Override
            public Option<String> generate(final HttpMethod method, final URI url, final URI addOnBaseUrl, final Map<String, String[]> parameters)
            {
                return Option.none(String.class);
            }
        };
    }
}
