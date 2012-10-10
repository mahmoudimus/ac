package com.atlassian.labs.remoteapps.plugin.module.webhook;

import com.atlassian.labs.remoteapps.plugin.RemoteAppAccessorFactory;
import com.atlassian.labs.remoteapps.plugin.util.uri.Uri;
import com.atlassian.labs.remoteapps.plugin.util.uri.UriBuilder;
import com.atlassian.labs.remoteapps.spi.webhook.PluginUriResolver;

import java.net.URI;

import static com.google.common.base.Preconditions.*;

public final class RemoteAppsPluginUriResolver implements PluginUriResolver
{
    private final RemoteAppAccessorFactory remoteAppAccessorFactory;

    public RemoteAppsPluginUriResolver(RemoteAppAccessorFactory remoteAppAccessorFactory)
    {
        this.remoteAppAccessorFactory = checkNotNull(remoteAppAccessorFactory);
    }

    @Override
    public URI getUri(String pluginKey, URI path)
    {
        return new UriBuilder(Uri.parse(remoteAppAccessorFactory.get(pluginKey).getDisplayUrl() + path.toString())).toUri().toJavaUri();
    }
}
