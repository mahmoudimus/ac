package com.atlassian.plugin.connect.plugin.webhooks;

import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.Request;
import com.atlassian.plugin.connect.plugin.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.plugin.HttpHeaderNames;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.util.BundleUtil;
import com.atlassian.plugin.connect.spi.ConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.spi.http.AuthorizationGenerator;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.webhooks.spi.plugin.RequestSigner;
import org.osgi.framework.BundleContext;

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
    private final ConnectAddOnIdentifierService jsonConnectAddOnIdentifierService;
    private final BundleContext bundleContext;

    @Inject
    public RemotePluginRequestSigner(DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory, JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService, BundleContext bundleContext)
    {
        this.remotablePluginAccessorFactory = checkNotNull(remotablePluginAccessorFactory);
        this.jsonConnectAddOnIdentifierService = checkNotNull(jsonConnectAddOnIdentifierService);
        this.bundleContext = checkNotNull(bundleContext);
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
            String version = BundleUtil.getBundleVersion(bundleContext);
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
        return jsonConnectAddOnIdentifierService.isConnectAddOn(pluginKey);
    }
}
