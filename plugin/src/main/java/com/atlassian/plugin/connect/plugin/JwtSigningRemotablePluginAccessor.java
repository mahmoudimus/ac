package com.atlassian.plugin.connect.plugin;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.applinks.JwtService;
import com.atlassian.jwt.applinks.exception.NotAJwtPeerException;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.util.http.HttpContentRetriever;
import com.atlassian.plugin.connect.spi.http.AuthorizationGenerator;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import com.google.common.base.Supplier;

import java.net.URI;
import java.util.Map;

import static com.atlassian.jwt.JwtConstants.AppLinks.SHARED_SECRET_PROPERTY_NAME;

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

    public JwtSigningRemotablePluginAccessor(ConnectAddonBean addon,
                                             Supplier<URI> baseUrlSupplier,
                                             JwtService jwtService,
                                             ConsumerService consumerService,
                                             ConnectApplinkManager connectApplinkManager,
                                             HttpContentRetriever httpContentRetriever,
                                             UserManager userManager)
    {
        super(addon.getKey(),addon.getName(), baseUrlSupplier, httpContentRetriever);
        this.jwtService = jwtService;
        this.consumerService = consumerService;
        this.connectApplinkManager = connectApplinkManager;
        this.userManager = userManager;
        this.authorizationGenerator = new JwtAuthorizationGenerator(jwtService, sharedSecretSupplier(getAppLink()), consumerService, URI.create(addon.getBaseUrl()));
    }

    @Override
    public String signGetUrl(URI targetPath, Map<String, String[]> params)
    {
        assertThatTargetPathAndParamsDoNotDuplicateParams(targetPath, params);

        String encodedJwt = JwtAuthorizationGenerator.encodeJwt(HttpMethod.GET, targetPath, getBaseUrl(), params, userManager, consumerService.getConsumer().getKey(), jwtService, requireSharedSecret(getAppLink()));
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

    private static Supplier<String> sharedSecretSupplier(final ApplicationLink applicationLink)
    {
        return new Supplier<String>()
        {
            @Override
            public String get()
            {
                return requireSharedSecret(applicationLink);
            }
        };
    }

    private static String requireSharedSecret(ApplicationLink applicationLink)
    {
        String sharedSecret = (String) applicationLink.getProperty(SHARED_SECRET_PROPERTY_NAME);

        if (sharedSecret == null)
        {
            throw new NotAJwtPeerException(applicationLink);
        }

        return sharedSecret;
    }
}
