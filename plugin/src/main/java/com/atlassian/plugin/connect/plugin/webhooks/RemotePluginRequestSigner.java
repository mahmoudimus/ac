package com.atlassian.plugin.connect.plugin.webhooks;

import com.atlassian.fugue.Effect;
import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.Request;
import com.atlassian.plugin.connect.plugin.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.spi.http.AuthorizationGenerator;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.webhooks.api.register.listener.WebHookListenerRegistrationDetails;
import com.atlassian.webhooks.spi.RequestSigner;

import java.net.URI;
import java.util.Collections;
import javax.inject.Inject;
import javax.inject.Named;

import static com.atlassian.jwt.JwtConstants.HttpRequests.AUTHORIZATION_HEADER;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Signs outgoing webhooks with oauth credentials
 */
@ExportAsService (RequestSigner.class)
@Named
public class RemotePluginRequestSigner implements RequestSigner
{
    private final DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final ConnectPluginIdentification connectPluginIdentification;
    private final ConnectAddonRegistry connectAddonRegistry;

    @Inject
    public RemotePluginRequestSigner(final DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory, final ConnectPluginIdentification connectPluginIdentification, final ConnectAddonRegistry connectAddonRegistry)
    {
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.connectPluginIdentification = connectPluginIdentification;
        this.connectAddonRegistry = connectAddonRegistry;
    }

    @Override
    public void sign(final URI uri, WebHookListenerRegistrationDetails registrationDetails, final Request.Builder request)
    {
        connectPluginIdentification.connectAddOnKey(registrationDetails).foreach(new Effect<String>()
        {
            @Override
            public void apply(final String addOnKey)
            {
                final Option<String> authValue = getAuthHeader(uri, addOnKey);
                if (authValue.isDefined())
                {
                    request.setHeader(AUTHORIZATION_HEADER, authValue.get());
                }
            }
        });
    }

    private Option<String> getAuthHeader(URI uri, String pluginKey)
    {
        return getAuthorizationGenerator(pluginKey).generate(HttpMethod.POST, uri, Collections.<String, String[]>emptyMap());
    }

    private AuthorizationGenerator getAuthorizationGenerator(String pluginKey)
    {
        return remotablePluginAccessorFactory.get(pluginKey).getAuthorizationGenerator();
    }

}
