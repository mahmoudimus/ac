package com.atlassian.plugin.connect.plugin.capabilities.event;

import java.net.URI;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Response;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.sal.api.ApplicationProperties;
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

import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Maps.newHashMap;

@Named
public class ConnectEventHandler implements InitializingBean, DisposableBean
{
    private static final Logger log = LoggerFactory.getLogger(ConnectEventHandler.class);
    private final EventPublisher eventPublisher;
    private final PluginEventManager pluginEventManager;
    private final PluginAccessor pluginAccessor;
    private final RemotablePluginAccessorFactory pluginAccessorFactory;
    private final UserManager userManager;
    private final HttpClient httpClient;
    private final RequestSigner requestSigner;
    private final ConsumerService consumerService;
    private final ApplicationProperties applicationProperties;
    private final ProductAccessor productAccessor;
    private final BundleContext bundleContext;

    @Inject
    public ConnectEventHandler(EventPublisher eventPublisher, PluginEventManager pluginEventManager, PluginAccessor pluginAccessor, RemotablePluginAccessorFactory pluginAccessorFactory, UserManager userManager, HttpClient httpClient, RequestSigner requestSigner, ConsumerService consumerService, ApplicationProperties applicationProperties, ProductAccessor productAccessor, BundleContext bundleContext)
    {
        this.eventPublisher = eventPublisher;
        this.pluginEventManager = pluginEventManager;
        this.pluginAccessor = pluginAccessor;
        this.pluginAccessorFactory = pluginAccessorFactory;
        this.userManager = userManager;
        this.httpClient = httpClient;
        this.requestSigner = requestSigner;
        this.consumerService = consumerService;
        this.applicationProperties = applicationProperties;
        this.productAccessor = productAccessor;
        this.bundleContext = bundleContext;
    }

    public void pluginInstalled(String pluginKey)
    {
        callSyncHandler(pluginKey);
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

    private void callSyncHandler(String pluginKey)
    {
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


                            URI installHandler = getURI(addonAccessor.getBaseUrl().toString() + path);

                            Request.Builder request = httpClient.newRequest(installHandler);
                            request.setAttribute("purpose", "web-hook-notification");
                            request.setAttribute("pluginKey", pluginKey);
                            request.setContentType(MediaType.APPLICATION_JSON);
                            request.setEntity(json);

                            //TODO: is there a better way to sign this?
                            requestSigner.sign(installHandler, pluginKey, request);

                            Response response = request.execute(Request.Method.POST).claim();
                            if (response.getStatusCode() != 200)
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
    }

    @VisibleForTesting
    Map<String, Object> newRemotePluginEventData()
    {
        final Consumer consumer = consumerService.getConsumer();

        ImmutableMap.Builder builder = ImmutableMap.<String, Object>builder()
                                                   .put("links", ImmutableMap.of(
                                                           "oauth", applicationProperties.getBaseUrl() + "/rest/atlassian-connect/latest/oauth"))
                                                   .put("clientKey", nullToEmpty(consumer.getKey()))
                                                   .put("publicKey", nullToEmpty(RSAKeys.toPemEncoding(consumer.getPublicKey())))
                                                   .put("serverVersion", nullToEmpty(applicationProperties.getBuildNumber()))
                                                   .put("pluginsVersion", nullToEmpty(getConnectPluginVersion()))
                                                   .put("baseUrl", nullToEmpty(applicationProperties.getBaseUrl()))
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

    private String getConnectPluginVersion()
    {
        Object bundleVersion = bundleContext.getBundle().getHeaders().get(Constants.BUNDLE_VERSION);
        return bundleVersion == null ? null : bundleVersion.toString();
    }
}
