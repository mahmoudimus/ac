package com.atlassian.plugin.connect.plugin.webhooks;

import com.atlassian.fugue.Effect;
import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.Request;
import com.atlassian.plugin.connect.plugin.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.plugin.HttpHeaderNames;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.util.BundleUtil;
import com.atlassian.plugin.connect.spi.http.AuthorizationGenerator;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.webhooks.api.register.listener.WebHookListenerRegistrationDetails;
import com.atlassian.webhooks.spi.RequestSigner;
import org.osgi.framework.BundleContext;

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
    private final ConnectPluginIdentifierService connectPluginIdentifierService;
    private final ConnectAddonRegistry connectAddonRegistry;
    private final BundleContext bundleContext;

    @Inject
    public RemotePluginRequestSigner(final DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory, final ConnectPluginIdentifierService connectPluginIdentifierService, final ConnectAddonRegistry connectAddonRegistry, final BundleContext bundleContext)
    {
        this.remotablePluginAccessorFactory = checkNotNull(remotablePluginAccessorFactory);
        this.connectPluginIdentifierService = checkNotNull(connectPluginIdentifierService);
        this.connectAddonRegistry = checkNotNull(connectAddonRegistry);
        this.bundleContext = checkNotNull(bundleContext);
    }

    @Override
    public void sign(final URI uri, WebHookListenerRegistrationDetails registrationDetails, final Request.Builder request)
    {
        connectPluginIdentifierService.connectAddOnKey(registrationDetails).foreach(new Effect<String>()
        {
            @Override
            public void apply(final String addOnKey)
            {
                final Option<String> authValue = getAuthHeader(uri, addOnKey);
                if (authValue.isDefined())
                {
                    request.setHeader(AUTHORIZATION_HEADER, authValue.get());
                }
                //Webhooks SPI does not provide any other extension points for adding headers
                //to requests, so we'll just do it here
                String version = BundleUtil.getBundleVersion(bundleContext);
                request.setHeader(HttpHeaderNames.ATLASSIAN_CONNECT_VERSION, version);
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
