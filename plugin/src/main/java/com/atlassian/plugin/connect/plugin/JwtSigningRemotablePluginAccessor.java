package com.atlassian.plugin.connect.plugin;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.applinks.JwtService;
import com.atlassian.jwt.core.TimeUtil;
import com.atlassian.jwt.core.writer.JsonSmartJwtJsonBuilder;
import com.atlassian.jwt.core.writer.JwtClaimsBuilder;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.jwt.writer.JwtJsonBuilder;
import com.atlassian.plugin.connect.plugin.util.http.HttpContentRetriever;
import com.atlassian.plugin.connect.spi.http.AuthorizationGenerator;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import com.google.common.base.Supplier;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class JwtSigningRemotablePluginAccessor extends DefaultRemotablePluginAccessorBase
{
    private final JwtService jwtService;
    private final ApplicationLinkAccessor applicationLinkAccessor;

    private static final Logger log = LoggerFactory.getLogger(JwtSigningRemotablePluginAccessor.class);
    private static final int JWT_EXPIRY_WINDOW_SECONDS = 60 * 3; // TODO: make a configuration option?
    private static final AuthorizationGenerator AUTH_GENERATOR = new JwtAuthorizationGenerator(); // it's tiny and does very little, so share this instance

    public JwtSigningRemotablePluginAccessor(String pluginKey,
                                             String pluginName,
                                             Supplier<URI> baseUrlSupplier,
                                             JwtService jwtService,
                                             ApplicationLinkAccessor applicationLinkAccessor,
                                             HttpContentRetriever httpContentRetriever)
    {
        super(pluginKey, pluginName, baseUrlSupplier, httpContentRetriever);
        this.jwtService = jwtService;
        this.applicationLinkAccessor = applicationLinkAccessor;
    }

    @Override
    public String signGetUrl(URI targetPath, Map<String, String[]> params)
    {
        assertThatTargetPathAndParamsDoNotDuplicateParams(targetPath, params);
        final UriBuilder uriBuilder = new UriBuilder(Uri.fromJavaUri(URI.create(createGetUrl(targetPath, params))));

        final ApplicationLink appLink = applicationLinkAccessor.getApplicationLink(getKey());
        JwtJsonBuilder jsonBuilder = new JsonSmartJwtJsonBuilder()
                .issuedAt(TimeUtil.currentTimeSeconds())
                .expirationTime(TimeUtil.currentTimePlusNSeconds(JWT_EXPIRY_WINDOW_SECONDS))
                .issuer(appLink.getId().get());

        try
        {
            JwtClaimsBuilder.appendHttpRequestClaims(jsonBuilder, new CanonicalHttpUriRequest(new HttpGet(uriBuilder.toString()), ""));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }

        String encodedJwt = jwtService.issueJwt(jsonBuilder.build(), appLink);
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
        return AUTH_GENERATOR;
    }
}
