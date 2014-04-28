package com.atlassian.plugin.connect.plugin.threeleggedauth;

import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.core.http.auth.SimplePrincipal;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.oauth.serviceprovider.ServiceProviderToken;
import com.atlassian.oauth.serviceprovider.ServiceProviderTokenStore;
import com.atlassian.oauth.serviceprovider.SystemClock;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserKey;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class OAuthConsumerThreeLeggedAuthService implements ThreeLeggedAuthService
{
    private final ServiceProviderTokenStore tokenStore;
    private final ConsumerService consumerService;
    private final ServiceProviderConsumerStore consumerStore;
    private final I18nResolver i18nResolver;

    private static final String AGENCY_DESC_I18N_KEY = "connect.scope.agent.description.personal";
    private static final Logger log = LoggerFactory.getLogger(OAuthConsumerThreeLeggedAuthService.class);

    @Autowired
    public OAuthConsumerThreeLeggedAuthService(ServiceProviderTokenStore tokenStore, ConsumerService consumerService, ServiceProviderConsumerStore consumerStore, I18nResolver i18nResolver)
    {
        this.tokenStore = checkNotNull(tokenStore);
        this.consumerService = checkNotNull(consumerService);
        this.consumerStore = checkNotNull(consumerStore);
        this.i18nResolver = checkNotNull(i18nResolver);
    }

    @Override
    public boolean grant(UserKey userKey, ConnectAddonBean addOnBean) throws NoAgentScopeException
    {
        if (!addOnBean.getScopes().contains(ScopeName.AGENT))
        {
            throw new NoAgentScopeException(addOnBean.getKey(), addOnBean.getScopes());
        }

        boolean granted = false;
        final Consumer consumer = getOrCreateConsumer(addOnBean);
        final ServiceProviderToken accessToken = findExtantAccessToken(userKey, consumer);

        if (!tokenIsValid(accessToken))
        {
            granted = true;
            tokenStore.put(createAccessToken(userKey, addOnBean, consumer));
        }

        return granted;
    }

    @Override
    public boolean hasGrant(UserKey userKey, ConnectAddonBean addOnBean)
    {
        boolean validGrantExists = false;

        if (addOnBean.getScopes().contains(ScopeName.AGENT))
        {
            final Consumer consumer = getConsumer(addOnBean);
            validGrantExists = null != consumer && tokenIsValid(findExtantAccessToken(userKey, consumer));
        }

        return validGrantExists;
    }

    @Override
    public void revokeAll(ConnectAddonBean addOnBean)
    {
        final Consumer consumer = getConsumer(addOnBean);

        if (null != consumer)
        {
            tokenStore.removeByConsumer(consumer.getKey());
            consumerStore.remove(consumer.getKey());
            log.debug("Removed consumer '{}' and its access tokens.", consumer.getKey());
        }
    }

    private boolean tokenIsValid(ServiceProviderToken accessToken)
    {
        return null != accessToken && accessToken.isAccessToken() && accessToken.hasBeenAuthorized() && !accessToken.hasBeenDenied() && !accessToken.hasExpired(new SystemClock());
    }

    private ServiceProviderToken findExtantAccessToken(UserKey userKey, Consumer consumer)
    {
        ServiceProviderToken token = null;

        for (ServiceProviderToken accessToken : tokenStore.getAccessTokensForUser(userKey.getStringValue()))
        {
            if (null != accessToken && accessToken.hasProperty(JwtConstants.HAS_AGENCY_PROPERTY_NAME) && consumer.getKey().equals(accessToken.getConsumer().getKey()))
            {
                token = accessToken;
                break;
            }
        }

        return token;
    }

    private ServiceProviderToken createAccessToken(UserKey user, ConnectAddonBean addOnBean, Consumer consumer)
    {
        log.debug("Creating new access token allowing add-on '{}' to act on behalf of user '{}'.", addOnBean.getKey(), user.getStringValue());
        return ServiceProviderToken.newAccessToken(UUID.randomUUID().toString())
                .authorizedBy(new SimplePrincipal(user.getStringValue()))
                .creationTime(System.currentTimeMillis())
                .timeToLive(365L * 24 * 60 * 60 * 1000) // 365 days
                .properties(ImmutableMap.of(JwtConstants.HAS_AGENCY_PROPERTY_NAME, "true"))
                .tokenSecret(UUID.randomUUID().toString())
                .consumer(consumer)
                .build();
    }

    private Consumer getOrCreateConsumer(ConnectAddonBean addOnBean)
    {
        // try to use a pre-existing consumer so that we create minimal data
        Consumer consumer = getConsumer(addOnBean);

        // fall back to creating a fake but non-null consumer if none exists: the OAuth plugin demands it
        if (null == consumer)
        {
            consumer = createConsumer(addOnBean);
        }

        return consumer;
    }

    private Consumer getConsumer(ConnectAddonBean addOnBean)
    {
        return consumerStore.get(getConsumerKey(addOnBean));
    }

    // don't stomp on normal oauth tokens created for this add-on, allow us to set a tla-specific description
    private String getConsumerKey(ConnectAddonBean addOnBean)
    {
        return "tla_" + addOnBean.getKey();
    }

    private Consumer createConsumer(ConnectAddonBean addOnBean)
    {
        String consumerKey = getConsumerKey(addOnBean);
        log.debug("Creating OAuth consumer '{}' for the three-legged-auth grants of add-on '{}'.", consumerKey, addOnBean.getKey());
        Consumer consumer = Consumer
                .key(consumerKey)
                .name(addOnBean.getName()) // displayed in the GUI
                .description(i18nResolver.getText(AGENCY_DESC_I18N_KEY)) // displayed in the GUI
                .signatureMethod(Consumer.SignatureMethod.HMAC_SHA1) // not used
                .publicKey(consumerService.getConsumer().getPublicKey()) // not used; set the product's because constructing public keys is balls
                .build();
        consumerStore.put(consumer); // because the OfBiz code later looks it up in the store by key
        return consumer;
    }
}
