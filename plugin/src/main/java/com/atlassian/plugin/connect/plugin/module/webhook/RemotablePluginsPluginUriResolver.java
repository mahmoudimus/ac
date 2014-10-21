package com.atlassian.plugin.connect.plugin.module.webhook;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.plugin.webhooks.ConnectPluginIdentifierService;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.webhooks.api.register.listener.WebHookListenerRegistrationDetails;
import com.atlassian.webhooks.spi.UriResolver;
import com.google.common.base.Function;

import java.net.URI;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
@ExportAsService
public final class RemotablePluginsPluginUriResolver implements UriResolver
{
    private final DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final ConnectPluginIdentifierService connectPluginIdentifierService;

    @Inject
    public RemotablePluginsPluginUriResolver(DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory, final ConnectPluginIdentifierService connectPluginIdentifierService)
    {
        this.connectPluginIdentifierService = connectPluginIdentifierService;
        this.remotablePluginAccessorFactory = checkNotNull(remotablePluginAccessorFactory);
    }

    @Override
    public Option<URI> getUri(final WebHookListenerRegistrationDetails listenerOriginDetails, final URI path)
    {
        return connectPluginIdentifierService.connectAddOnKey(listenerOriginDetails).map(new Function<String, URI>()
        {
            @Override
            public URI apply(@Nullable final String addOnKey)
            {
                return remotablePluginAccessorFactory.get(addOnKey).getTargetUrl(path);

            }
        });
    }
}
