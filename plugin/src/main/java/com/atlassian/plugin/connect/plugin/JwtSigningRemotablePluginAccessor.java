package com.atlassian.plugin.connect.plugin;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.applinks.JwtService;
import com.atlassian.jwt.core.TimeUtil;
import com.atlassian.jwt.core.writer.JsonSmartJwtJsonBuilder;
import com.atlassian.jwt.core.writer.JwtClaimsBuilder;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.jwt.writer.JwtJsonBuilder;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.util.ConfigurationUtils;
import com.atlassian.plugin.connect.plugin.util.http.HttpContentRetriever;
import com.atlassian.plugin.connect.spi.http.AuthorizationGenerator;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import com.google.common.base.Supplier;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Constructs and signs outgoing URLs using the JWT protocol.<p> See {@link JwtService} for more details.<p> Set the
 * system property {@link #JWT_EXPIRY_SECONDS_PROPERTY} with an integer value to control the size of the expiry window
 * (default is {@link #JWT_EXPIRY_WINDOW_SECONDS_DEFAULT}).
 */
public class JwtSigningRemotablePluginAccessor extends DefaultRemotablePluginAccessorBase
{
    private final JwtService jwtService;
    private final ConsumerService consumerService;
    private final ConnectApplinkManager connectApplinkManager;
    private final AuthorizationGenerator authorizationGenerator;

    private static final String JWT_EXPIRY_SECONDS_PROPERTY = "com.atlassian.connect.jwt.expiry_seconds";
    /**
     * Default of 3 minutes.
     */
    private static final int JWT_EXPIRY_WINDOW_SECONDS_DEFAULT = 60 * 3;
    private static final int JWT_EXPIRY_WINDOW_SECONDS = ConfigurationUtils.getIntSystemProperty(JWT_EXPIRY_SECONDS_PROPERTY, JWT_EXPIRY_WINDOW_SECONDS_DEFAULT);

    public JwtSigningRemotablePluginAccessor(String pluginKey,
                                             String pluginName,
                                             Supplier<URI> baseUrlSupplier,
                                             JwtService jwtService,
                                             ConsumerService consumerService,
                                             ConnectApplinkManager connectApplinkManager,
                                             HttpContentRetriever httpContentRetriever)
    {
        super(pluginKey, pluginName, baseUrlSupplier, httpContentRetriever);
        this.jwtService = jwtService;
        this.consumerService = consumerService;
        this.connectApplinkManager = connectApplinkManager;
        this.authorizationGenerator = new JwtAuthorizationGenerator(jwtService, getAppLink());
    }

    private ApplicationLink getAppLink()
    {
        return this.connectApplinkManager.getAppLink(getKey());
    }

    @Override
    public String signGetUrl(URI targetPath, Map<String, String[]> params)
    {
        assertThatTargetPathAndParamsDoNotDuplicateParams(targetPath, params);

        JwtJsonBuilder jsonBuilder = new JsonSmartJwtJsonBuilder()
                .issuedAt(TimeUtil.currentTimeSeconds())
                .expirationTime(TimeUtil.currentTimePlusNSeconds(JWT_EXPIRY_WINDOW_SECONDS))
                .issuer(consumerService.getConsumer().getKey());

        try
        {
            JwtClaimsBuilder.appendHttpRequestClaims(jsonBuilder, new CanonicalHttpUriRequest(HttpMethod.GET.toString(), targetPath.getPath(), "", params));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }

        String encodedJwt = jwtService.issueJwt(jsonBuilder.build(), getAppLink());
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
}
