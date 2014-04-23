package com.atlassian.plugin.connect.plugin.threeleggedauth.servlet;

import com.atlassian.jwt.core.http.auth.SimplePrincipal;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.oauth.serviceprovider.ServiceProviderToken;
import com.atlassian.oauth.serviceprovider.ServiceProviderTokenStore;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonBeanFactory;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class GrantThreeLeggedAuthServlet extends HttpServlet
{
    private final JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService;
    private final ConnectAddonRegistry connectAddonRegistry;
    private final ConnectAddonBeanFactory connectAddonBeanFactory;
    private final UserManager userManager;
    private final ServiceProviderTokenStore tokenStore;
    private final ConsumerService consumerService;
    private final ServiceProviderConsumerStore consumerStore;
    private final I18nResolver i18nResolver;

    private static final String AGENCY_PROP_NAME = "connect.scope.agent";
    private static final String AGENCY_DESC_I18N_KEY = "connect.scope.agent.description.personal";
    private static final String AGENCY_DESC_WITH_ADD_ON_I18N_KEY = "connect.scope.agent.description.personal.withkey";
    private static final String EXTANT_I18N_KEY = "connect.prefix.extant";
    private static final String GRANTED_I18N_KEY = "connect.prefix.granted";
    private static final Pattern PATH_PATTERN = Pattern.compile("^(/[^/]+){3}/([^/]+)"); // .../ac/tla/grant/{add-on-key}
    private static final Logger log = LoggerFactory.getLogger(GrantThreeLeggedAuthServlet.class);

    @Autowired
    public GrantThreeLeggedAuthServlet(JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService,
                                       ConnectAddonRegistry connectAddonRegistry,
                                       ConnectAddonBeanFactory connectAddonBeanFactory,
                                       UserManager userManager,
                                       ServiceProviderTokenStore tokenStore,
                                       ConsumerService consumerService,
                                       ServiceProviderConsumerStore consumerStore,
                                       I18nResolver i18nResolver)
    {
        this.jsonConnectAddOnIdentifierService = checkNotNull(jsonConnectAddOnIdentifierService);
        this.connectAddonRegistry = checkNotNull(connectAddonRegistry);
        this.connectAddonBeanFactory = checkNotNull(connectAddonBeanFactory);
        this.userManager = checkNotNull(userManager);
        this.tokenStore = checkNotNull(tokenStore);
        this.consumerService = checkNotNull(consumerService);
        this.consumerStore = checkNotNull(consumerStore);
        this.i18nResolver = checkNotNull(i18nResolver);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        UserKey user = userManager.getRemoteUserKey(req);

        // accessible only to logged-in users so that we don't leak installed-add-on-key knowledge to anonymous randoms
        if (null == user)
        {
            resp.sendRedirect(getLoginUri(getUri(req)).toASCIIString());
        }
        else
        {
            Matcher matcher = PATH_PATTERN.matcher(req.getPathInfo());

            if (matcher.find())
            {
                String addOnKey = matcher.group(2);

                if (jsonConnectAddOnIdentifierService.isConnectAddOn(addOnKey))
                {
                    String actionPrefixI18nKey = EXTANT_I18N_KEY;

                    if (null == findExtantAccessToken(user))
                    {
                        tokenStore.put(createAccessToken(user, getOrCreateConsumer(addOnKey), addOnKey));
                        actionPrefixI18nKey = GRANTED_I18N_KEY;
                    }

                    resp.setContentType("text/html;charset=UTF-8");
                    resp.getWriter().append(String.format("<html><body><h1>Three-Legged Auth</h1><p>%s %s</p></body></html>",
                            i18nResolver.getText(actionPrefixI18nKey), i18nResolver.getText(AGENCY_DESC_WITH_ADD_ON_I18N_KEY, addOnKey)))
                            .flush();
                    resp.setStatus(HttpServletResponse.SC_OK);
                }
                else
                {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, String.format("'%s' is not an installed Connect add-on key", addOnKey));
                }
            }
            else
            {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    private Consumer getOrCreateConsumer(String addOnKey)
    {
        final String consumerKey = "tla_" + addOnKey; // don't stomp on normal oauth tokens created for this add-on, allow us to set a tla-specific description

        // try to use a pre-existing consumer so that we create minimal data
        Consumer consumer = consumerStore.get(consumerKey);

        // fall back to creating a fake but non-null consumer if none exists: the OAuth plugin demands it
        if (null == consumer)
        {
            consumer = createConsumer(consumerKey, addOnKey);
        }

        return consumer;
    }

    private ServiceProviderToken findExtantAccessToken(UserKey user)
    {
        ServiceProviderToken token = null;
        final Iterable<ServiceProviderToken> userAccessTokens =  tokenStore.getAccessTokensForUser(user.getStringValue());

        for (ServiceProviderToken accessToken : userAccessTokens)
        {
            if (null != accessToken && accessToken.hasProperty(AGENCY_PROP_NAME))
            {
                token = accessToken;
                break;
            }
        }

        return token;
    }

    private static ServiceProviderToken createAccessToken(UserKey user, Consumer consumer, String addOnKey)
    {
        log.debug("Creating new access token allowing add-on '{}' to act on behalf of user '{}'.", addOnKey, user.getStringValue());
        return ServiceProviderToken.newAccessToken(UUID.randomUUID().toString())
                                .authorizedBy(new SimplePrincipal(user.getStringValue()))
                                .creationTime(System.currentTimeMillis())
                                .timeToLive(365L * 24 * 60 * 60 * 1000) // 365 days
                                .properties(ImmutableMap.of(AGENCY_PROP_NAME, "true"))
                                .tokenSecret(UUID.randomUUID().toString())
                                .consumer(consumer)
                                .build();
    }

    private Consumer createConsumer(String consumerKey, String addOnKey)
    {
        log.debug("Creating OAuth consumer '{}' for the three-legged-auth grants of add-on '{}'.", consumerKey, addOnKey);
        ConnectAddonBean addOnBean = connectAddonBeanFactory.fromJsonSkipValidation(connectAddonRegistry.getDescriptor(addOnKey));
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

    private URI getLoginUri(URI destinationUri) throws UnsupportedEncodingException
    {
        // TODO: surely there has to be some service that we can call to get the login URL
        final String destinationUriString = destinationUri.toASCIIString();
        return URI.create(destinationUriString.replaceAll("/plugins/.*", "/login.jsp?permissionViolation=true&os_destination=" + URLEncoder.encode(destinationUriString, "UTF-8")));
    }

    private URI getUri(HttpServletRequest request)
    {
        StringBuffer builder = request.getRequestURL();
        
        if (request.getQueryString() != null)
        {
            builder.append("?");
            builder.append(request.getQueryString());
        }

        return URI.create(builder.toString());
    }
}
