package com.atlassian.plugin.connect.plugin.module.webhook;

import java.net.URI;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.plugin.connect.plugin.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import com.atlassian.webhooks.spi.plugin.PluginUriResolver;

import com.google.common.base.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
@ExportAsService
public final class RemotablePluginsPluginUriResolver implements PluginUriResolver
{
    private final DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory;

    @Inject
    public RemotablePluginsPluginUriResolver(DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory)
    {
        this.remotablePluginAccessorFactory = checkNotNull(remotablePluginAccessorFactory);
    }

    @Override
    public Optional<URI> getUri(String pluginKey, URI path)
    {
        if (!path.isAbsolute())
        {
            return Optional.of(new UriBuilder(Uri.parse(remotablePluginAccessorFactory.get(pluginKey).getBaseUrl() + path.toString())).toUri().toJavaUri());
        }
        return Optional.absent();
    }
}
