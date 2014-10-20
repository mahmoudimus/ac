package com.atlassian.plugin.connect.plugin;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.auth.types.OAuthAuthenticationProvider;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.fugue.Option;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.Request;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.plugin.connect.plugin.applinks.DefaultConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.util.OAuthHelper;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.OAuthServiceProvider;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;
import net.oauth.signature.RSA_SHA1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Manages oauth link operations
 */
@Component
@com.atlassian.plugin.connect.api.xmldescriptor.OAuth
public class OAuthLinkManager
{

    public static final String OAUTH_INCOMING_CONSUMERKEY = "oauth.incoming.consumerkey";
    public static final String CONSUMER_KEY_OUTBOUND = "consumerKey.outbound";
    public static final String SERVICE_PROVIDER_REQUEST_TOKEN_URL = "serviceProvider.requestTokenUrl";
    public static final String SERVICE_PROVIDER_ACCESS_TOKEN_URL = "serviceProvider.accessTokenUrl";
    public static final String SERVICE_PROVIDER_AUTHORIZE_URL = "serviceProvider.authorizeUrl";

    private static final Logger log = LoggerFactory.getLogger(OAuthLinkManager.class);
    private final ServiceProviderConsumerStore serviceProviderConsumerStore;
    private final AuthenticationConfigurationManager authenticationConfigurationManager;
    private final ConsumerService consumerService;
    private final OAuthValidator oauthValidator;

    @Autowired
    public OAuthLinkManager(ServiceProviderConsumerStore serviceProviderConsumerStore,
                            AuthenticationConfigurationManager authenticationConfigurationManager,
                            ConsumerService consumerService)
    {
        this.serviceProviderConsumerStore = serviceProviderConsumerStore;
        this.authenticationConfigurationManager = authenticationConfigurationManager;
        this.consumerService = consumerService;
        this.oauthValidator = new SimpleOAuthValidator();
    }

    public Option<PublicKey> getPublicKeyFromLink(ApplicationLink link)
    {
        Object prop = link.getProperty(OAUTH_INCOMING_CONSUMERKEY);
        if(prop != null && prop instanceof String)
        {
            Consumer consumer = this.serviceProviderConsumerStore.get((String) prop);
            if(consumer != null)
            {
                return Option.option(consumer.getPublicKey());
            }
        }
        return Option.none();
    }

    public void associateConsumerWithLink(ApplicationLink link, Consumer consumer)
    {
        unassociateConsumer(consumer);

        // this logic was copied from ual
        serviceProviderConsumerStore.put(consumer);
        link.putProperty(OAUTH_INCOMING_CONSUMERKEY, consumer.getKey());
    }

    public void unassociateConsumer(Consumer consumer)
    {
        String key = consumer.getKey();
        if (serviceProviderConsumerStore.get(key) != null)
        {
            serviceProviderConsumerStore.remove(key);
        }
    }

    public boolean isAppAssociated(String appKey)
    {
        return serviceProviderConsumerStore.get(appKey) != null;
    }

    public void associateProviderWithLink(ApplicationLink link, String key, ServiceProvider serviceProvider)
    {
        unassociateProviderWithLink(link);
        authenticationConfigurationManager.registerProvider(link.getId(), OAuthAuthenticationProvider.class,
                ImmutableMap.of(CONSUMER_KEY_OUTBOUND, key, SERVICE_PROVIDER_REQUEST_TOKEN_URL,
                        serviceProvider.getRequestTokenUri().toString(), SERVICE_PROVIDER_ACCESS_TOKEN_URL,
                        serviceProvider.getAccessTokenUri().toString(), SERVICE_PROVIDER_AUTHORIZE_URL,
                        serviceProvider.getAccessTokenUri().toString()));
    }

    public void unassociateProviderWithLink(ApplicationLink link)
    {
        if (authenticationConfigurationManager.isConfigured(link.getId(), OAuthAuthenticationProvider.class))
        {
            authenticationConfigurationManager.unregisterProvider(link.getId(), OAuthAuthenticationProvider.class);
        }
    }

    public void validateOAuth2LORequest(OAuthMessage message) throws IOException, URISyntaxException, OAuthException
    {
        String consumerKey = message.getConsumerKey();
        Consumer consumer = serviceProviderConsumerStore.get(consumerKey);
        if (consumer == null)
        {
            Consumer self = consumerService.getConsumer();
            if (self.getKey().equals(consumerKey))
            {
                consumer = self;
            }
            else
            {
                throw new OAuthProblemException(OAuth.Problems.CONSUMER_KEY_UNKNOWN);
            }
        }
        final OAuthConsumer oauthConsumer = new OAuthConsumer(null, consumer.getKey(), null,
                new OAuthServiceProvider(null, null, null));
        oauthConsumer.setProperty(RSA_SHA1.PUBLIC_KEY, consumer.getPublicKey().getEncoded());
        final OAuthAccessor accessor = new OAuthAccessor(oauthConsumer);
        if (log.isDebugEnabled())
        {
            printMessageToDebug(message);
        }
        oauthValidator.validateMessage(message, accessor);
    }

    private static String appLinkToAddOnKey(ApplicationLink link)
    {
        final Object addOnKeyProperty = link.getProperty(DefaultConnectApplinkManager.PLUGIN_KEY_PROPERTY);
        return null == addOnKeyProperty ? null : addOnKeyProperty.toString();
    }

    private void printMessageToDebug(OAuthMessage message) throws IOException
    {
        StringBuilder sb = new StringBuilder("Validating incoming OAuth 2LO request:\n");
        sb.append("\turl: ").append(message.URL).append("\n");
        sb.append("\tmethod: ").append(message.method).append("\n");
        for (Map.Entry<String, String> entry : message.getParameters())
        {
            sb.append("\t").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        log.debug(sb.toString());
    }

    public String generateAuthorizationHeader(HttpMethod method,
                                              ServiceProvider serviceProvider,
                                              URI url,
                                              Map<String, List<String>> originalParams)
    {
        final OAuthMessage message = sign(serviceProvider, method, checkNotNull(url), originalParams);

        try
        {
            return message.getAuthorizationHeader(null);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public List<Map.Entry<String, String>> signAsParameters(ServiceProvider serviceProvider,
                                                            HttpMethod method,
                                                            URI url,
                                                            Map<String, List<String>> originalParams
    )
    {
        OAuthMessage message = sign(serviceProvider, method, url, originalParams);
        if (message != null)
        {
            try
            {
                return message.getParameters();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            return newArrayList(Maps.transformValues(originalParams, new Function<List<String>, String>()
            {
                @Override
                public String apply(List<String> strings)
                {
                    // TODO: Doesn't handle multiple values with the same param name
                    return strings.get(0);
                }
            }).entrySet());
        }
    }

    @VisibleForTesting
    public OAuthMessage sign(ServiceProvider serviceProvider,
                              HttpMethod method,
                              URI url,
                              Map<String, List<String>> originalParams
    )
    {
        notNull(serviceProvider);
        notNull(url);
        checkNormalized(url);

        Map<String, List<String>> params = newHashMap(originalParams);
        Consumer self = consumerService.getConsumer();
        params.put(OAuth.OAUTH_CONSUMER_KEY, singletonList(self.getKey()));
        if (log.isDebugEnabled())
        {
            dumpParamsToSign(params);
        }
        Request oAuthRequest = new Request(Request.HttpMethod.valueOf(method.name()), url, convertParameters(params));
        final Request signedRequest = consumerService.sign(oAuthRequest, serviceProvider);
        return OAuthHelper.asOAuthMessage(signedRequest);
    }

    private static void checkNormalized(URI url)
    {
        if (!url.normalize().getPath().equals(url.getPath()))
        {
            throw new IllegalArgumentException("Refusing to sign non-normalized URL: " + url.toString());
        }
    }

    private void dumpParamsToSign(Map<String, List<String>> params)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Signing outgoing with: \n");
        for (Map.Entry<String, List<String>> entry : params.entrySet())
        {
            sb.append("\t").append(entry.getKey()).append(": ").append(entry.getValue().toString()).append("\n");
        }
        log.debug(sb.toString());
    }

    private List<com.atlassian.oauth.Request.Parameter> convertParameters(Map<String, List<String>> reqParameters)
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
}
