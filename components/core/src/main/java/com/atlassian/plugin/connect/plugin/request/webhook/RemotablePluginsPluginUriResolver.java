package com.atlassian.plugin.connect.plugin.request.webhook;

import com.atlassian.plugin.connect.plugin.ConnectAddonRegistry;
import com.atlassian.plugin.connect.api.request.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.webhooks.spi.plugin.PluginUriResolver;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
@ExportAsService
public final class RemotablePluginsPluginUriResolver implements PluginUriResolver
{
    private final DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final ConnectAddonRegistry connectAddonRegistry;

    @Inject
    public RemotablePluginsPluginUriResolver(DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory,
            ConnectAddonRegistry connectAddonRegistry)
    {
        this.connectAddonRegistry = connectAddonRegistry;
        this.remotablePluginAccessorFactory = checkNotNull(remotablePluginAccessorFactory);
    }

    @Override
    public Optional<URI> getUri(String pluginKey, URI path)
    {
        if (!path.isAbsolute() && connectAddonRegistry.hasAddonWithKey(pluginKey))
        {
            return Optional.of(remotablePluginAccessorFactory.get(pluginKey).getTargetUrl(path));
        }
        return Optional.absent();
    }
}
