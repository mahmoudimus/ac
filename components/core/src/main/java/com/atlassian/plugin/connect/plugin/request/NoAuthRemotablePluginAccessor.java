package com.atlassian.plugin.connect.plugin.request;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.auth.AuthorizationGenerator;
import com.atlassian.plugin.connect.api.request.HttpContentRetriever;
import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

import com.google.common.base.Supplier;

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
            public Optional<String> generate(final HttpMethod method, final URI url, final Map<String, String[]> parameters)
            {
                return Optional.empty();
            }
        };
    }
}
