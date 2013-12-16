package com.atlassian.plugin.connect.plugin;

import com.atlassian.fugue.Option;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.util.http.HttpContentRetriever;
import com.atlassian.plugin.connect.spi.PermissionDeniedException;
import com.atlassian.plugin.connect.spi.http.AuthorizationGenerator;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import net.oauth.OAuth;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.atlassian.fugue.Option.option;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.transformValues;
import static java.util.Collections.singletonList;

public class NoAuthRemotablePluginAccessor extends DefaultRemotablePluginAccessorBase
{
    public NoAuthRemotablePluginAccessor(Plugin plugin, Supplier<URI> baseUrl, HttpContentRetriever httpContentRetriever)
    {
        super(plugin, baseUrl, httpContentRetriever);
    }

    @Override
    public String signGetUrl(URI targetPath, Map<String, String[]> params)
    {
        return createGetUrl(targetPath, params);
    }

    @Override
    public AuthorizationGenerator getAuthorizationGenerator()
    {
        return new DefaultAuthorizationGeneratorBase()
        {
            @Override
            public Option<String> generate(final HttpMethod method, final URI url, final Map<String, List<String>> parameters)
            {
                return Option.none(String.class);
            }
        };
    }
}
