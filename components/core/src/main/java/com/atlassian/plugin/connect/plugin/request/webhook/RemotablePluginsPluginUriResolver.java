package com.atlassian.plugin.connect.plugin.request.webhook;

import com.atlassian.plugin.connect.api.ConnectAddonAccessor;
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
    private final ConnectAddonAccessor addonAccessor;

    @Inject
    public RemotablePluginsPluginUriResolver(DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory,
            ConnectAddonAccessor addonAccessor)
    {
        this.addonAccessor = addonAccessor;
        this.remotablePluginAccessorFactory = checkNotNull(remotablePluginAccessorFactory);
    }

    @Override
    public Optional<URI> getUri(String pluginKey, URI path)
    {
        if (!path.isAbsolute() && addonAccessor.getAddon(pluginKey).isPresent())
        {
            return Optional.of(remotablePluginAccessorFactory.get(pluginKey).getTargetUrl(path));
        }
        return Optional.absent();
    }
}
