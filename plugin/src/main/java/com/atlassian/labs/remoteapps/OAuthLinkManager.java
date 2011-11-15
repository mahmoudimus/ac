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
import net.oauth.*;
import net.oauth.signature.RSA_SHA1;
import org.apache.axis.encoding.ser.ElementSerializer;
import org.netbeans.lib.cvsclient.commandLine.command.log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.singletonList;

/**
 * Manages oauth link operations
 */
@Component
public class OAuthLinkManager
{

    public static final String CONSUMER_KEY_OUTBOUND = "consumerKey.outbound";
    public static final String SERVICE_PROVIDER_REQUEST_TOKEN_URL = "serviceProvider.requestTokenUrl";
    public static final String SERVICE_PROVIDER_ACCESS_TOKEN_URL = "serviceProvider.accessTokenUrl";
    public static final String SERVICE_PROVIDER_AUTHORIZE_URL = "serviceProvider.authorizeUrl";

    private static final Logger log = LoggerFactory.getLogger(OAuthLinkManager.class);
    private final ServiceProviderConsumerStore serviceProviderConsumerStore;
    private final AuthenticationConfigurationManager authenticationConfigurationManager;
    private final ApplicationLinkService applicationLinkService;
    private final ConsumerService consumerService;
    private final UserManager userManager;
    private final ApplicationProperties applicationProperties;
    private final OAuthValidator oauthValidator;

    @Autowired
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
        this.oauthValidator = new SimpleOAuthValidator();
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

    public void validateOAuth2LORequest(OAuthMessage message) throws IOException, URISyntaxException, OAuthException
    {
        String consumerKey = message.getConsumerKey();
        Consumer consumer = serviceProviderConsumerStore.get(consumerKey);
        final OAuthConsumer oauthConsumer = new OAuthConsumer(null,
                consumer.getKey(),
                null,
                new OAuthServiceProvider(null, null, null));
        oauthConsumer.setProperty(RSA_SHA1.PUBLIC_KEY, consumer.getPublicKey().getEncoded());
        final OAuthAccessor accessor = new OAuthAccessor(oauthConsumer);
        if (log.isDebugEnabled())
        {
            printMessageToDebug(message);
        }
        oauthValidator.validateMessage(message, accessor);
    }

    private void printMessageToDebug(OAuthMessage message) throws IOException
    {
        StringBuilder sb = new StringBuilder("Validating incoming OAuth 2LO request:\n");
        sb.append("\turl: ").append(message.URL).append("\n");
        sb.append("\tmethod: ").append(message.method).append("\n");
        for (Map.Entry<String,String> entry : message.getParameters())
        {
            sb.append("\t").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        log.debug(sb.toString());
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
        Consumer self = consumerService.getConsumer();
        params.put(OAuth.OAUTH_CONSUMER_KEY, singletonList(self.getKey()));
        params.put("user_id", singletonList(currentUser));
        params.put("xdm_e", singletonList(host));
        params.put("xdm_c", singletonList("channel01"));
        params.put("xdm_p", singletonList("1"));
        if (log.isDebugEnabled())
        {
            dumpParamsToSign(params);
        }
        ServiceProvider serviceProvider = getServiceProvider(link);
        Request oAuthRequest = new Request(Request.HttpMethod.valueOf(method),
                URI.create(url), convertParameters(params));
        final com.atlassian.oauth.Request signedRequest = consumerService.sign(oAuthRequest, serviceProvider);
        return OAuthHelper.asOAuthMessage(signedRequest);
    }

    private void dumpParamsToSign(Map<String, List<String>> params)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Sigining outgoing with: \n");
        for (Map.Entry<String,List<String>> entry : params.entrySet())
        {
            sb.append("\t").append(entry.getKey()).append(": ").append(entry.getValue().toString()).append("\n");
        }
        log.debug(sb.toString());
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
