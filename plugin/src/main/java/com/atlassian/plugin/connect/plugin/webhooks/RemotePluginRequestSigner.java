package com.atlassian.plugin.connect.plugin.webhooks;

import com.atlassian.fugue.Effect;
import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.Request;
import com.atlassian.plugin.connect.plugin.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.service.LegacyAddOnIdentifierService;
import com.atlassian.plugin.connect.spi.ConnectAddOnIdentifierService;
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
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonKeyOnly;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Signs outgoing webhooks with oauth credentials
 */
@ExportAsService (RequestSigner.class)
@Named
public class RemotePluginRequestSigner implements RequestSigner
{
    private final DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final ConnectAddOnIdentifierService jsonConnectAddOnIdentifierService;
    private final ConnectAddOnIdentifierService legacyAddOnIdentifierService;
    private final ConnectAddonRegistry connectAddonRegistry;

    @Inject
    public RemotePluginRequestSigner(DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory, JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService, LegacyAddOnIdentifierService legacyAddOnIdentifierService,
            ConnectAddonRegistry connectAddonRegistry)
    {
        this.remotablePluginAccessorFactory = checkNotNull(remotablePluginAccessorFactory);
        this.jsonConnectAddOnIdentifierService = checkNotNull(jsonConnectAddOnIdentifierService);
        this.legacyAddOnIdentifierService = checkNotNull(legacyAddOnIdentifierService);
        this.connectAddonRegistry = checkNotNull(connectAddonRegistry);
    }

    @Override
    public void sign(final URI uri, WebHookListenerRegistrationDetails registrationDetails, final Request.Builder request)
    {
        registrationDetails.getModuleDescriptorDetails().foreach(new Effect<WebHookListenerRegistrationDetails.ModuleDescriptorRegistrationDetails>()
        {
            @Override
            public void apply(final WebHookListenerRegistrationDetails.ModuleDescriptorRegistrationDetails registrationDetails)
            {
                String addOnKey = addonKeyOnly(registrationDetails.getModuleKey().orNull());
                if (canSign(addOnKey))
                {
                    final Option<String> authValue = getAuthHeader(uri, addOnKey);
                    if (authValue.isDefined())
                    {
                        request.setHeader(AUTHORIZATION_HEADER, authValue.get());
                    }
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

    // return true if this is a Connect add-on
    private boolean canSign(final String pluginKey)
    {
        return jsonConnectAddOnIdentifierService.isConnectAddOn(pluginKey) || legacyAddOnIdentifierService.isConnectAddOn(pluginKey);
    }
}
