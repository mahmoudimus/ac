package com.atlassian.plugin.connect.plugin.scopes;

import java.util.EnumSet;
import java.util.Set;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.connect.api.scopes.AddOnKeyExtractor;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.sal.api.lifecycle.LifecycleAware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.servlet.http.HttpServletRequest;

/**
 * Class allowing for extracting of plugin key from http requests.
 */
@Component
public class AddOnKeyExtractorImpl implements AddOnKeyExtractor, InitializingBean, DisposableBean, LifecycleAware
{
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Set by a {@link javax.servlet.Filter}, possibly using {@link com.atlassian.plugin.connect.plugin.module.oauth.OAuth2LOAuthenticator} or {@link com.atlassian.jwt.plugin.sal.JwtAuthenticator},
     * indicating the Connect add-on that is the origin of the current request.
     */
    private static final String PLUGIN_KEY_ATTRIBUTE = JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME;

    /**
     * The key of this atlassian-connect plugig.
     */
    private static final String PLUGIN_KEY = "com.atlassian.plugins.atlassian-connect-plugin";

    private final JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService;
    private final ConsumerService consumerService;
    private String ourConsumerKey;

    private final EventPublisher eventPublisher;
    @GuardedBy ("this")
    private final Set<LifecycleEvent> lifecycleEvents = EnumSet.noneOf(LifecycleEvent.class);

    @Autowired
    public AddOnKeyExtractorImpl(final JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService, ConsumerService consumerService, EventPublisher eventPublisher)
    {
        this.jsonConnectAddOnIdentifierService = jsonConnectAddOnIdentifierService;
        this.consumerService = consumerService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Nullable
    public String getAddOnKeyFromHttpRequest(@Nonnull HttpServletRequest req)
    {
        String addOnKey = extractClientKey(req);
        if (addOnKey != null)
        {
            return addOnKey;
        }
        addOnKey = extractXdmRequestKey(req);
        if (addOnKey != null && jsonConnectAddOnIdentifierService.isConnectAddOn(addOnKey))
        {
            return addOnKey;
        }
        return null;
    }

    @Override
    public boolean isAddOnRequest(@Nonnull HttpServletRequest request)
    {
        String addOnKey = extractClientKey(request);
        return (addOnKey != null && !ourConsumerKey.equals(addOnKey)) || (extractXdmRequestKey(request) != null);
    }

    @Override
    @Nullable
    public String extractClientKey(@Nonnull HttpServletRequest req)
    {
        return (String) req.getAttribute(PLUGIN_KEY_ATTRIBUTE);
    }


    @Override
    public void setClientKey(@Nonnull HttpServletRequest req, @Nonnull String clientKey)
    {
        req.setAttribute(PLUGIN_KEY_ATTRIBUTE, clientKey);
    }

    /**
     * @param req the context {@link javax.servlet.http.HttpServletRequest}
     * @return a {@link #AP_REQUEST_HEADER header} set by the XDM host library, indicating the current request is a host-mediated XHR sent on
     *         behalf of an add-on running in a sandboxed iframe; see AP.request(...) in the host-side AP js
     */
    @Nullable
    private static String extractXdmRequestKey(HttpServletRequest req)
    {
        return req.getHeader(AP_REQUEST_HEADER);
    }

    @EventListener
    public void onPluginEnabled(PluginEnabledEvent event)
    {
        if (PLUGIN_KEY.equals(event.getPlugin().getKey()))
        {
            onLifecycleEvent(LifecycleEvent.PLUGIN_ENABLED);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        eventPublisher.register(this);
        onLifecycleEvent(LifecycleEvent.AFTER_PROPERTIES_SET);
    }

    @Override
    public void onStart()
    {
        onLifecycleEvent(LifecycleEvent.LIFECYCLE_AWARE_ON_START);
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }

    /**
     * Wait for both PluginEnableEvent and onStart before performing any initialization that relating to DB or external
     * service. The need of this come from a bug in plugins in platform 2 SAL-282, onStart is called too early.
     * go/awesome-launcher for more information. After platform 3, we don't need this kind of 3-2-1-GO! any more, just
     * to use onStart.
     */
    private void onLifecycleEvent(LifecycleEvent event)
    {
        logger.info("onLifecycleEvent {}", event);
        if (isLifecycleReady(event))
        {
            logger.info("Got the last lifecycle event... Time to get started!");
            eventPublisher.unregister(this);
            finalInit();
        }
    }

    synchronized private boolean isLifecycleReady(LifecycleEvent event)
    {
        return lifecycleEvents.add(event) && lifecycleEvents.size() == LifecycleEvent.values().length;
    }

    protected void finalInit()
    {
        this.ourConsumerKey = consumerService.getConsumer().getKey();
    }

    static enum LifecycleEvent
    {
        AFTER_PROPERTIES_SET,
        PLUGIN_ENABLED,
        LIFECYCLE_AWARE_ON_START
    }
}
