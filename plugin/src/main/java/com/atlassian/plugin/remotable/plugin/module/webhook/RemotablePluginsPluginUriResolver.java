package com.atlassian.plugin.remotable.plugin.module.webhook;

import com.atlassian.plugin.remotable.plugin.RemotablePluginAccessorFactory;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import com.atlassian.webhooks.spi.plugin.PluginUriResolver;

import java.net.URI;

import static com.google.common.base.Preconditions.*;

public final class RemotablePluginsPluginUriResolver implements PluginUriResolver
{
    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;

    public RemotablePluginsPluginUriResolver(RemotablePluginAccessorFactory remotablePluginAccessorFactory)
    {
        this.remotablePluginAccessorFactory = checkNotNull(remotablePluginAccessorFactory);
    }

    @Override
    public URI getUri(String pluginKey, URI path)
    {
        return new UriBuilder(Uri.parse(remotablePluginAccessorFactory.get(pluginKey).getDisplayUrl() + path.toString())).toUri().toJavaUri();
    }
}
