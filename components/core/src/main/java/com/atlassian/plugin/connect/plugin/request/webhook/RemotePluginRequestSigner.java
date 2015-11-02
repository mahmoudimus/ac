package com.atlassian.plugin.connect.plugin.request.webhook;

import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.Request;
import com.atlassian.plugin.connect.api.ConnectAddonAccessor;
import com.atlassian.plugin.connect.api.auth.AuthorizationGenerator;
import com.atlassian.plugin.connect.api.request.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.api.request.HttpHeaderNames;
import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.webhooks.spi.plugin.RequestSigner;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;
import java.util.Collections;

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
    private final ConnectAddonAccessor addonAccessor;
    private final PluginRetrievalService pluginRetrievalService;

    @Inject
    public RemotePluginRequestSigner(DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory,
            ConnectAddonAccessor addonAccessor,
            PluginRetrievalService pluginRetrievalService)
    {
        this.remotablePluginAccessorFactory = checkNotNull(remotablePluginAccessorFactory);
        this.addonAccessor = addonAccessor;
        this.pluginRetrievalService = pluginRetrievalService;
    }

    @Override
    public void sign(URI uri, String pluginKey, Request.Builder request)
    {
        if (canSign(pluginKey))
        {
            final Option<String> authValue = getAuthHeader(uri, pluginKey);
            if (authValue.isDefined())
            {
                request.setHeader(AUTHORIZATION_HEADER, authValue.get());
            }
            //Webhooks SPI does not provide any other extension points for adding headers
            //to requests, so we'll just do it here
            String version = pluginRetrievalService.getPlugin().getPluginInformation().getVersion();
            request.setHeader(HttpHeaderNames.ATLASSIAN_CONNECT_VERSION, version);
        }
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
        return addonAccessor.getAddon(pluginKey).isPresent();
    }
}
