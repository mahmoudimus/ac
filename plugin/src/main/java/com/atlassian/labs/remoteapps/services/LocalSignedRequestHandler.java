package com.atlassian.labs.remoteapps.services;

import com.atlassian.labs.remoteapps.OAuthLinkManager;
import com.atlassian.labs.remoteapps.api.services.SignedRequestHandler;
import com.atlassian.labs.remoteapps.api.services.impl.AbstractOauthSignedRequestHandler;
import com.atlassian.labs.remoteapps.loader.universalbinary.UBDispatchFilter;
import com.atlassian.labs.remoteapps.modules.oauth.OAuth2LOAuthenticator;
import com.atlassian.labs.remoteapps.util.OAuthHelper;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.Request;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.bridge.Consumers;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthServiceProvider;
import net.oauth.server.OAuthServlet;
import net.oauth.signature.RSA_SHA1;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * Created with IntelliJ IDEA. User: mrdon Date: 7/27/12 Time: 3:08 PM To change this template use
 * File | Settings | File Templates.
 */
public class LocalSignedRequestHandler extends AbstractOauthSignedRequestHandler implements SignedRequestHandler
{
    private final UBDispatchFilter ubDispatchFilter;
    private final ApplicationProperties applicationProperties;
    private final ConsumerService consumerService;
    private final String pluginKey;

    public LocalSignedRequestHandler(UBDispatchFilter ubDispatchFilter,
            ApplicationProperties applicationProperties,
            ConsumerService consumerService, String pluginKey)
    {
        this.ubDispatchFilter = ubDispatchFilter;
        this.applicationProperties = applicationProperties;
        this.consumerService = consumerService;
        this.pluginKey = pluginKey;
    }

    @Override
    public String getHostBaseUrl(String key)
    {
        // we ignore the key as you can only have one client when ran in-process
        return applicationProperties.getBaseUrl();
    }

    @Override
    public String getAuthorizationHeaderValue(String uri, String method,
            String username) throws IllegalArgumentException
    {
        Set<Request.Parameter> params = username != null ? ImmutableSet.of(new Request.Parameter("user_id", username))
                            : Collections.<Request.Parameter>emptySet();
        Request request = new Request(Request.HttpMethod.valueOf(method), URI.create(uri), params);
        URI dummyUri = URI.create("http://localhost");
        OAuthMessage message = OAuthHelper.asOAuthMessage(consumerService.sign(request,
                new ServiceProvider(dummyUri, dummyUri, dummyUri)));

        try
        {
            return message.getAuthorizationHeader(null);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("shouldn't happen", e);
        }
    }

    @Override
    public String getLocalBaseUrl()
    {
        return ubDispatchFilter.getLocalMountBaseUrl(pluginKey);
    }

    @Override
    protected Object getHostOauthPublicKey(String key)
    {
        // always return the host
        return consumerService.getConsumer().getPublicKey().getEncoded();
    }

}
