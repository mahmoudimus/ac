package com.atlassian.labs.remoteapps;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.auth.types.OAuthAuthenticationProvider;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.labs.remoteapps.util.OAuthHelper;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.Request;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableMap;
import net.oauth.OAuth;
import net.oauth.OAuthMessage;
import org.apache.axis.encoding.ser.ElementSerializer;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.singletonList;

/**
 *
 */
public class OAuthLinkManager
{

    public static final String CONSUMER_KEY_OUTBOUND = "consumerKey.outbound";
    public static final String SERVICE_PROVIDER_REQUEST_TOKEN_URL = "serviceProvider.requestTokenUrl";
    public static final String SERVICE_PROVIDER_ACCESS_TOKEN_URL = "serviceProvider.accessTokenUrl";
    public static final String SERVICE_PROVIDER_AUTHORIZE_URL = "serviceProvider.authorizeUrl";
    private final ServiceProviderConsumerStore serviceProviderConsumerStore;
    private final AuthenticationConfigurationManager authenticationConfigurationManager;
    private final ApplicationLinkService applicationLinkService;
    private final ConsumerService consumerService;
    private final UserManager userManager;
    private final ApplicationProperties applicationProperties;

    public OAuthLinkManager(ServiceProviderConsumerStore serviceProviderConsumerStore,
                            AuthenticationConfigurationManager authenticationConfigurationManager,
                            ApplicationLinkService applicationLinkService,
                            ConsumerService consumerService,
                            UserManager userManager,
                            ApplicationProperties applicationProperties)
    {
        this.serviceProviderConsumerStore = serviceProviderConsumerStore;
        this.authenticationConfigurationManager = authenticationConfigurationManager;
        this.applicationLinkService = applicationLinkService;
        this.consumerService = consumerService;
        this.userManager = userManager;
        this.applicationProperties = applicationProperties;
    }

    public void associateConsumerWithLink(ApplicationLink link, Consumer consumer)
    {
        String key = consumer.getKey();
        if (serviceProviderConsumerStore.get(key) != null)
        {
            serviceProviderConsumerStore.remove(key);
        }

        // fixme: this logic was copied from ual
        serviceProviderConsumerStore.put(consumer);
        link.putProperty("oauth.incoming.consumerkey", consumer.getKey());
    }

    public void associateProviderWithLink(ApplicationLink link, String key, ServiceProvider serviceProvider)
    {
        if (authenticationConfigurationManager.isConfigured(link.getId(), OAuthAuthenticationProvider.class))
        {
            authenticationConfigurationManager.unregisterProvider(link.getId(), OAuthAuthenticationProvider.class);
        }
        authenticationConfigurationManager.registerProvider(
            link.getId(),
            OAuthAuthenticationProvider.class,
            ImmutableMap.of(CONSUMER_KEY_OUTBOUND, key,
                    SERVICE_PROVIDER_REQUEST_TOKEN_URL, serviceProvider.getRequestTokenUri().toString(),
                    SERVICE_PROVIDER_ACCESS_TOKEN_URL, serviceProvider.getAccessTokenUri().toString(),
                    SERVICE_PROVIDER_AUTHORIZE_URL, serviceProvider.getAccessTokenUri().toString()
            ));
    }

    public ServiceProvider getServiceProvider(ApplicationLink link)
    {
        Map<String,String> config = authenticationConfigurationManager.getConfiguration(link.getId(), OAuthAuthenticationProvider.class);
        if (config.containsKey(CONSUMER_KEY_OUTBOUND))
        {
            final String accessTokenUrl = config.get(SERVICE_PROVIDER_ACCESS_TOKEN_URL);
            final String requestTokenUrl = config.get(SERVICE_PROVIDER_REQUEST_TOKEN_URL);
            final String authorizeUrl = config.get(SERVICE_PROVIDER_AUTHORIZE_URL);
            return new ServiceProvider(URI.create(requestTokenUrl), URI.create(authorizeUrl), URI.create(accessTokenUrl));
        }
        else
        {
            throw new IllegalStateException("Should have oauth configured");
        }
    }

    public OAuthMessage sign(ApplicationLink link, String method, String url, Map<String,List<String>> originalParams)
    {
        URI hostUri = URI.create(applicationProperties.getBaseUrl());
        String host = hostUri.getScheme() + "://" + hostUri.getHost() + ":" + hostUri.getPort();
        String currentUser = userManager.getRemoteUsername();
        Map<String,List<String>> params = newHashMap(originalParams);
        params.put("user_id", singletonList(currentUser));
        params.put("xdm_e", singletonList(host));
        params.put("xdm_c", singletonList("channel01"));
        params.put("xdm_p", singletonList("1"));
        ServiceProvider serviceProvider = getServiceProvider(link);
        Request oAuthRequest = new Request(Request.HttpMethod.valueOf(method),
                URI.create(url), convertParameters(params));
        final com.atlassian.oauth.Request signedRequest = consumerService.sign(oAuthRequest, serviceProvider);
        return OAuthHelper.asOAuthMessage(signedRequest);
    }

    private List<com.atlassian.oauth.Request.Parameter> convertParameters(Map<String,List<String>> reqParameters)
    {
        final List<com.atlassian.oauth.Request.Parameter> parameters = new ArrayList<Request.Parameter>();
        for (final String parameterName : reqParameters.keySet())
        {
            final List<String> values = reqParameters.get(parameterName);
            for (final String value : values)
            {
                parameters.add(new com.atlassian.oauth.Request.Parameter(parameterName, value));
            }
        }
        return parameters;
    }

    public ApplicationLink getLinkForOAuthClientKey(String clientKey)
    {
        // todo: optimise
        for (ApplicationLink link : applicationLinkService.getApplicationLinks())
        {
            if (clientKey.equals(link.getProperty("oauth.incoming.consumerkey")))
            {
                return link;
            }
        }
        // todo: handle this better
        throw new IllegalArgumentException();
    }
}
