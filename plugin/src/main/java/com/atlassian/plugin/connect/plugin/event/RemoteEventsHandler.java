package com.atlassian.plugin.connect.plugin.event;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Response;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.plugin.service.LegacyAddOnIdentifierService;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.event.RemotePluginEnabledEvent;
import com.atlassian.plugin.connect.spi.event.RemotePluginInstalledEvent;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.spi.PluginInstallException;
import com.atlassian.uri.UriBuilder;
import com.atlassian.webhooks.spi.plugin.RequestSigner;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;

//import com.atlassian.plugin.event.events.BeforePluginDisabledEvent;

/**
 * For handling legacy xml-descriptor add-ons. See {@link com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager} for JSON-descriptor add-ons.
 */
@Component
public class RemoteEventsHandler implements InitializingBean, DisposableBean
{
    private static final Logger log = LoggerFactory.getLogger(RemoteEventsHandler.class);
    private static final String HTTP_ERROR_I18N_KEY_PREFIX = "connect.install.error.remote.host.bad.response.";
    private static final List<Integer> OK_INSTALL_HTTP_CODES = asList(200, 201, 204);

    private final EventPublisher eventPublisher;
    private final PluginEventManager pluginEventManager;
    private final ConsumerService consumerService;
    private final ApplicationProperties applicationProperties;
    private final ProductAccessor productAccessor;
    private final BundleContext bundleContext;
    private final LegacyAddOnIdentifierService connectIdentifier;
    private final PluginAccessor pluginAccessor;
    private final RemotablePluginAccessorFactory pluginAccessorFactory;
    private final RequestSigner requestSigner;
    private final HttpClient httpClient;
    private final UserManager userManager;
    private final I18nResolver i18nResolver;

    @Autowired
    public RemoteEventsHandler(EventPublisher eventPublisher,
                               ConsumerService consumerService,
                               ApplicationProperties applicationProperties,
                               ProductAccessor productAccessor,
                               BundleContext bundleContext,
                               PluginEventManager pluginEventManager,
                               LegacyAddOnIdentifierService connectIdentifier,
                               PluginAccessor pluginAccessor,
                               RemotablePluginAccessorFactory pluginAccessorFactory,
                               RequestSigner requestSigner,
                               HttpClient httpClient,
                               UserManager userManager,
                               I18nResolver i18nResolver)
    {
        this.pluginAccessor = pluginAccessor;
        this.pluginAccessorFactory = pluginAccessorFactory;
        this.requestSigner = requestSigner;
        this.httpClient = httpClient;
        this.userManager = userManager;
        this.consumerService = checkNotNull(consumerService);
        this.applicationProperties = checkNotNull(applicationProperties);
        this.eventPublisher = checkNotNull(eventPublisher);
        this.pluginEventManager = checkNotNull(pluginEventManager);
        this.productAccessor = checkNotNull(productAccessor);
        this.bundleContext = checkNotNull(bundleContext);
        this.connectIdentifier = checkNotNull(connectIdentifier);
        this.i18nResolver = checkNotNull(i18nResolver);
    }

    public void pluginInstalled(String pluginKey)
    {
        //if we have an "install-handler" in plugin info, do a sync call, otherwise fallback to the webhook
        if (!callSyncHandler(pluginKey))
        {
            eventPublisher.publish(new RemotePluginInstalledEvent(checkNotNull(pluginKey), newRemotePluginEventData()));
        }
    }

    private boolean callSyncHandler(String pluginKey)
    {
        boolean called = false;
        Option<String> errorI18nKey = Option.<String>some("connect.remote.upm.install.exception");
        try
        {
            Plugin addon = pluginAccessor.getPlugin(pluginKey);

            if (null != addon)
            {
                Map<String, String> params = addon.getPluginInformation().getParameters();
                if (params.containsKey("install-handler"))
                {
                    String path = params.get("install-handler");
                    if (!Strings.isNullOrEmpty(path))
                    {
                        RemotablePluginAccessor addonAccessor = pluginAccessorFactory.get(pluginKey);

                        if (null != addonAccessor)
                        {
                            Map<String, Object> data = newHashMap(newRemotePluginEventData());
                            data.put("key", pluginKey);

                            String json = new JSONObject(data).toString(2);

                            URI installHandler = addonAccessor.getTargetUrl(getURI(path));

                            Request.Builder request = httpClient.newRequest(installHandler);
                            request.setAttribute("purpose", "web-hook-notification");
                            request.setAttribute("pluginKey", pluginKey);
                            request.setContentType(MediaType.APPLICATION_JSON);
                            request.setEntity(json);

                            requestSigner.sign(installHandler, pluginKey, request);

                            Response response = request.execute(Request.Method.POST).claim();

                            // a selection of 2xx response codes both indicate success and are semantically valid for this callback
                            if (OK_INSTALL_HTTP_CODES.contains(response.getStatusCode()))
                            {
                                called = true;
                            }
                            // it failed: if it failed with a response code for which we have a pre-canned i18n message then send that key
                            // so that UPM will display the message in the GUI
                            else
                            {
                                final String i18nKey = HTTP_ERROR_I18N_KEY_PREFIX + response.getStatusCode();
                                final String i18nText = i18nResolver.getRawText(i18nKey);

                                // the I18nResolver javadoc says that it will return the input key as the output raw text when the key is not found
                                if (null != i18nText && !i18nKey.equals(i18nText))
                                {
                                    errorI18nKey = Option.some(i18nKey);
                                }
                                // no else{}: fall back to the generic i18n key initialised at the start of the message
                            }

                            if (!called)
                            {
                                log.error("Error contacting remote application [" + response.getStatusText() + "]");
                                throw new PluginInstallException("Error contacting remote application [" + response.getStatusText() + "]", errorI18nKey);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error("Error contacting remote application [" + e.getMessage() + "]", e);
            throw new PluginInstallException("Error contacting remote application [" + e.getMessage() + "]", errorI18nKey);
        }

        return called;
    }

    private URI getURI(String url)
    {
        UriBuilder builder = new UriBuilder().setPath(url);

        UserProfile user = userManager.getRemoteUser();
        if (null != user)
        {
            builder.addQueryParameter("user_id", user.getUsername())
                   .addQueryParameter("user_key", user.getUserKey().getStringValue());
        }

        return builder.toUri().toJavaUri();
    }

    @PluginEventListener
    public void pluginEnabled(PluginEnabledEvent pluginEnabledEvent)
    {
        final Plugin plugin = pluginEnabledEvent.getPlugin();
        if (connectIdentifier.isConnectAddOn(plugin))
        {
            eventPublisher.publish(new RemotePluginEnabledEvent(plugin.getKey(), newRemotePluginEventData()));
        }
    }

//    @PluginEventListener
//    public void beforePluginDisabled(BeforePluginDisabledEvent pluginDisabledEvent)
//    {
//        final Plugin plugin = pluginDisabledEvent.getPlugin();
//        if (connectIdentifier.isConnectAddOn(plugin))
//        {
//            eventPublisher.publish(new RemotePluginDisabledEvent(plugin.getKey(), newRemotePluginEventData()));
//        }
//    }

    @VisibleForTesting
    Map<String, Object> newRemotePluginEventData()
    {
        final Consumer consumer = consumerService.getConsumer();

        ImmutableMap.Builder builder = ImmutableMap.<String, Object>builder()
                                                   .put("links", ImmutableMap.of(
                                                           "oauth", applicationProperties.getBaseUrl(UrlMode.CANONICAL) + "/rest/atlassian-connect/latest/oauth"))
                                                   .put("clientKey", nullToEmpty(consumer.getKey()))
                                                   .put("publicKey", nullToEmpty(RSAKeys.toPemEncoding(consumer.getPublicKey())))
                                                   .put("serverVersion", nullToEmpty(applicationProperties.getBuildNumber()))
                                                   .put("pluginsVersion", nullToEmpty(getRemotablePluginsPluginVersion()))
                                                   .put("baseUrl", nullToEmpty(applicationProperties.getBaseUrl(UrlMode.CANONICAL)))
                                                   .put("productType", nullToEmpty(productAccessor.getKey()))
                                                   .put("description", nullToEmpty(consumer.getDescription()));

        UserProfile user = userManager.getRemoteUser();
        if (null != user)
        {
            builder.put("user_id", user.getUsername())
                   .put("user_key", user.getUserKey().getStringValue());
        }

        return builder.build();
    }

    private String getRemotablePluginsPluginVersion()
    {
        Object bundleVersion = bundleContext.getBundle().getHeaders().get(Constants.BUNDLE_VERSION);
        return bundleVersion == null ? null : bundleVersion.toString();
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        this.pluginEventManager.register(this);
    }

    @Override
    public void destroy() throws Exception
    {
        this.pluginEventManager.unregister(this);
    }
}
