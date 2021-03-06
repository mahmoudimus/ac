package com.atlassian.plugin.connect.plugin.auth.oauth;

import com.atlassian.oauth.ServiceProvider;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.auth.AuthorizationGenerator;
import com.atlassian.plugin.connect.api.request.HttpContentRetriever;
import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.connect.api.util.UriBuilderUtils;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.PermissionDeniedException;
import com.atlassian.plugin.connect.plugin.request.DefaultRemotablePluginAccessorBase;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import net.oauth.OAuth;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Maps.transformValues;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;

public class OAuthSigningRemotablePluginAccessor extends DefaultRemotablePluginAccessorBase {
    private final ServiceProvider serviceProvider;
    private final OAuthLinkManager oAuthLinkManager;

    public OAuthSigningRemotablePluginAccessor(Plugin plugin,
                                               Supplier<URI> baseUrl,
                                               ServiceProvider serviceProvider,
                                               HttpContentRetriever httpContentRetriever,
                                               OAuthLinkManager oAuthLinkManager) {
        super(plugin, baseUrl, httpContentRetriever);
        this.serviceProvider = serviceProvider;
        this.oAuthLinkManager = oAuthLinkManager;
    }

    public OAuthSigningRemotablePluginAccessor(ConnectAddonBean addon,
                                               Supplier<URI> baseUrl,
                                               ServiceProvider serviceProvider,
                                               HttpContentRetriever httpContentRetriever,
                                               OAuthLinkManager oAuthLinkManager) {
        super(addon.getKey(), addon.getName(), baseUrl, httpContentRetriever);
        this.serviceProvider = serviceProvider;
        this.oAuthLinkManager = oAuthLinkManager;
    }

    @Override
    public String signGetUrl(URI targetPath, Map<String, String[]> params) {
        return signGetUrlForType(serviceProvider, getTargetUrl(targetPath), params);
    }

    @Override
    public AuthorizationGenerator getAuthorizationGenerator() {
        return new OAuthAuthorizationGenerator(serviceProvider, oAuthLinkManager);
    }

    private String signGetUrlForType(ServiceProvider serviceProvider, URI targetUrl, Map<String, String[]> params) throws PermissionDeniedException {
        final UriBuilder uriBuilder = new UriBuilder(Uri.fromJavaUri(targetUrl));

        // adding all the parameters of the signed request
        for (Map.Entry<String, String> param : signRequest(serviceProvider, targetUrl, params, HttpMethod.GET)) {
            final String value = param.getValue() == null ? "" : param.getValue();
            uriBuilder.addQueryParameter(param.getKey(), value);
        }
        return uriBuilder.toString();
    }

    private List<Map.Entry<String, String>> signRequest(ServiceProvider serviceProvider,
                                                        URI url,
                                                        Map<String, String[]> queryParams,
                                                        HttpMethod method) {
        String timestamp = System.currentTimeMillis() / 1000 + "";
        String nonce = System.nanoTime() + "";
        String signatureMethod = OAuth.RSA_SHA1;
        String oauthVersion = "1.0";

        Map<String, List<String>> params = Maps.newLinkedHashMap(transformValues(queryParams, Arrays::asList));

        params.put(OAuth.OAUTH_SIGNATURE_METHOD, singletonList(signatureMethod));
        params.put(OAuth.OAUTH_NONCE, singletonList(nonce));
        params.put(OAuth.OAUTH_VERSION, singletonList(oauthVersion));
        params.put(OAuth.OAUTH_TIMESTAMP, singletonList(timestamp));

        return oAuthLinkManager.signAsParameters(serviceProvider, method, url, params);
    }

    private class OAuthAuthorizationGenerator implements AuthorizationGenerator {
        private final ServiceProvider serviceProvider;
        private final OAuthLinkManager oAuthLinkManager;

        private OAuthAuthorizationGenerator(ServiceProvider serviceProvider, OAuthLinkManager oAuthLinkManager) {
            this.serviceProvider = serviceProvider;
            this.oAuthLinkManager = oAuthLinkManager;
        }

        public Optional<String> generate(HttpMethod method, URI url, Map<String, String[]> parameters) {
            return ofNullable(oAuthLinkManager.generateAuthorizationHeader(method, serviceProvider, url, UriBuilderUtils.toListFormat(parameters)));
        }
    }
}
