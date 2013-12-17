package com.atlassian.plugin.connect.plugin;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.applinks.JwtService;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.util.http.HttpContentRetriever;
import com.atlassian.plugin.connect.spi.http.AuthorizationGenerator;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import com.google.common.base.Supplier;

import java.net.URI;
import java.util.Map;

/**
 * Constructs and signs outgoing URLs using the JWT protocol.
 * See {@link JwtService} and {@link JwtAuthorizationGenerator} for more details.
 */
public class JwtSigningRemotablePluginAccessor extends DefaultRemotablePluginAccessorBase
{
    private final JwtService jwtService;
    private final ConsumerService consumerService;
    private final ConnectApplinkManager connectApplinkManager;
    private final UserManager userManager;
    private final AuthorizationGenerator authorizationGenerator;

    public JwtSigningRemotablePluginAccessor(String pluginKey,
                                             String pluginName,
                                             Supplier<URI> baseUrlSupplier,
                                             JwtService jwtService,
                                             ConsumerService consumerService,
                                             ConnectApplinkManager connectApplinkManager,
                                             HttpContentRetriever httpContentRetriever,
                                             UserManager userManager)
    {
        super(pluginKey, pluginName, baseUrlSupplier, httpContentRetriever);
        this.jwtService = jwtService;
        this.consumerService = consumerService;
        this.connectApplinkManager = connectApplinkManager;
        this.userManager = userManager;
        this.authorizationGenerator = new JwtAuthorizationGenerator(jwtService, getAppLink(), consumerService);
    }

    @Override
    public String signGetUrl(URI targetPath, Map<String, String[]> params)
    {
        assertThatTargetPathAndParamsDoNotDuplicateParams(targetPath, params);

        UserKey userKey = userManager.getRemoteUserKey();
        String userKeyValue = userKey == null ? "" : userKey.getStringValue();
        String encodedJwt = JwtAuthorizationGenerator.encodeJwt(HttpMethod.GET, targetPath, params, userKeyValue, consumerService.getConsumer().getKey(), jwtService, getAppLink());
        final UriBuilder uriBuilder = new UriBuilder(Uri.fromJavaUri(URI.create(createGetUrl(targetPath, params))));
        uriBuilder.addQueryParameter(JwtConstants.JWT_PARAM_NAME, encodedJwt);

        return uriBuilder.toString();
    }

    @Override
    public String createGetUrl(URI targetPath, Map<String, String[]> params)
    {
        assertThatTargetPathAndParamsDoNotDuplicateParams(targetPath, params);
        return super.createGetUrl(targetPath, params);
    }

    @Override
    public AuthorizationGenerator getAuthorizationGenerator()
    {
        return authorizationGenerator;
    }

    private ApplicationLink getAppLink()
    {
        return this.connectApplinkManager.getAppLink(getKey());
    }
}
