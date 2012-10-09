package com.atlassian.labs.remoteapps.plugin.webhook;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.plugin.util.tracker.WaitableServiceTrackerCustomizer;
import com.atlassian.labs.remoteapps.plugin.util.tracker.WaitableServiceTrackerFactory;
import com.atlassian.labs.remoteapps.plugin.webhook.impl.WebHookRegistrarImpl;
import com.atlassian.labs.remoteapps.spi.webhook.WebHookProvider;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.*;

/**
 * Manages web hook registrations and handles event dispatching
 */
public final class WebHookRegistrationManager implements DisposableBean, WebHookIdsAccessor
{
    private static final Logger log = LoggerFactory.getLogger(WebHookRegistrationManager.class);

    private final Map<String,WebHookRegistration> registrationsByKey;
    private final SetMultimap<Class<?>,WebHookRegistration> registrationsByEvent;
    private final Map<WebHookProvider, Set<WebHookRegistration>> registrationsByProvider;
    private final WebHookPublisher webHookPublisher;
    private final EventPublisher eventPublisher;

    @Autowired
    public WebHookRegistrationManager(WaitableServiceTrackerFactory factory, WebHookPublisher webHookPublisher, EventPublisher eventPublisher)
    {
        this.webHookPublisher = checkNotNull(webHookPublisher);
        this.eventPublisher = checkNotNull(eventPublisher);
        this.registrationsByEvent = Multimaps.synchronizedSetMultimap(HashMultimap.<Class<?>, WebHookRegistration>create());
        this.registrationsByKey = new ConcurrentHashMap<String, WebHookRegistration>();
        this.registrationsByProvider = new ConcurrentHashMap<WebHookProvider,Set<WebHookRegistration>>();

        factory.create(WebHookProvider.class, new WaitableServiceTrackerCustomizer<WebHookProvider>()
        {
            @Override
            public WebHookProvider adding(WebHookProvider service)
            {
                WebHookRegistrarImpl registrar = new WebHookRegistrarImpl();
                service.provide(registrar);
                for (WebHookRegistration reg : registrar.getRegistrations())
                {
                    if (reg.getEventClass() != null)
                    {
                        registrationsByEvent.put(reg.getEventClass(), reg);
                    }
                    registrationsByKey.put(reg.getId(), reg);
                }
                registrationsByProvider.put(service, registrar.getRegistrations());
                return service;
            }

            @Override
            public void removed(WebHookProvider service)
            {
                Set<WebHookRegistration> registrations = registrationsByProvider.remove(service);
                for (Iterator<WebHookRegistration> i = registrationsByEvent.values().iterator(); i.hasNext(); )
                {
                    if (registrations.contains(i.next()))
                    {
                        i.remove();
                    }
                }

                for (Iterator<WebHookRegistration> i = registrationsByKey.values().iterator(); i.hasNext(); )
                {
                    if (registrations.contains(i.next()))
                    {
                        i.remove();
                    }
                }
            }
        });
        this.eventPublisher.register(this);
    }

    @EventListener
    public void onEvent(Object event)
    {
        try
        {
            Iterable<WebHookRegistration> registrations = registrationsByEvent.get(event.getClass());
            if (registrations != null)
            {
                for (WebHookRegistration reg : registrations)
                {
                    webHookPublisher.publish(reg.getId(), reg.getEventMatcher(), reg.getEventSerializer(event));
                }
            }
        }
        catch (Exception e)
        {
            // Trap exceptions to prevent them bubbling up outside this event listener
            log.warn(String.format("Failed to publish web-hooks for event %s", event.getClass().getName()), e);
        }
    }
    
    public Iterable<String> getIds()
    {
        return registrationsByKey.keySet();
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }
}
