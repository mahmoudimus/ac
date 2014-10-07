package com.atlassian.plugin.connect.plugin.module.webhook;

import com.atlassian.plugin.connect.plugin.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.webhooks.api.register.listener.WebHookListenerRegistrationDetails;
import com.atlassian.webhooks.spi.UriResolver;
import com.google.common.base.*;

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

    @Inject
    public RemotablePluginsPluginUriResolver(DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory)
    {
        this.remotablePluginAccessorFactory = checkNotNull(remotablePluginAccessorFactory);
    }

    @Override
    public Optional<URI> getUri(final WebHookListenerRegistrationDetails listenerOriginDetails, final URI path)
    {
        return listenerOriginDetails.getModuleDescriptorDetails().fold(new Supplier<Optional<URI>>()
        {
            @Override
            public Optional<URI> get()
            {
                return Optional.absent();
            }
        }, new Function<WebHookListenerRegistrationDetails.ModuleDescriptorRegistrationDetails, Optional<URI>>()
        {
            @Override
            public Optional<URI> apply(@Nullable final WebHookListenerRegistrationDetails.ModuleDescriptorRegistrationDetails registrationDetails)
            {
                return Optional.of(remotablePluginAccessorFactory.get(registrationDetails.getPluginKey()).getTargetUrl(path));
            }
        });
    }
}
