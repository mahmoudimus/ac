package com.atlassian.labs.remoteapps;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.auth.types.OAuthAuthenticationProvider;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.google.common.collect.ImmutableMap;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
public class OAuthLinkManager
{

    private final ServiceProviderConsumerStore serviceProviderConsumerStore;
    private final AuthenticationConfigurationManager authenticationConfigurationManager;
    private final ApplicationLinkService applicationLinkService;

    public OAuthLinkManager(ServiceProviderConsumerStore serviceProviderConsumerStore, AuthenticationConfigurationManager authenticationConfigurationManager, ApplicationLinkService applicationLinkService)
    {
        this.serviceProviderConsumerStore = serviceProviderConsumerStore;
        this.authenticationConfigurationManager = authenticationConfigurationManager;
        this.applicationLinkService = applicationLinkService;
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
            ImmutableMap.of(
                    "consumerKey.outbound", key,
                    "serviceProvider.requestTokenUrl", serviceProvider.getRequestTokenUri().toString(),
                    "serviceProvider.accessTokenUrl", serviceProvider.getAccessTokenUri().toString(),
                    "serviceProvider.authorizeUrl", serviceProvider.getAccessTokenUri().toString()
            ));
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
