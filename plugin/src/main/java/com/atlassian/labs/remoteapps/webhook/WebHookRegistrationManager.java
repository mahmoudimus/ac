package com.atlassian.labs.remoteapps.webhook;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.util.tracker.WaitableServiceTracker;
import com.atlassian.labs.remoteapps.util.tracker.WaitableServiceTrackerCustomizer;
import com.atlassian.labs.remoteapps.util.tracker.WaitableServiceTrackerFactory;
import com.atlassian.labs.remoteapps.webhook.external.WebHookProvider;
import com.atlassian.labs.remoteapps.webhook.impl.WebHookRegistrarImpl;
import com.google.common.base.Predicate;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages web hook registrations and handles event dispatching
 */
@Component
public class WebHookRegistrationManager implements DisposableBean
{
    private final Map<String,WebHookRegistration> registrationsByKey;
    private final Map<Class<?>,WebHookRegistration> registrationsByEvent;
    private final Map<WebHookProvider, Set<WebHookRegistration>> registrationsByProvider;
    private final WaitableServiceTracker<WebHookProvider,WebHookProvider> waitableServiceTracker;
    private final WebHookPublisher webHookPublisher;
    private final EventPublisher eventPublisher;

    @Autowired
    public WebHookRegistrationManager(WaitableServiceTrackerFactory factory,
            WebHookPublisher webHookPublisher, EventPublisher eventPublisher)
    {
        this.webHookPublisher = webHookPublisher;
        this.eventPublisher = eventPublisher;
        this.registrationsByEvent = new ConcurrentHashMap<Class<?>, WebHookRegistration>();
        this.registrationsByKey = new ConcurrentHashMap<String, WebHookRegistration>();
        this.registrationsByProvider = new ConcurrentHashMap<WebHookProvider,
                Set<WebHookRegistration>>();
        this.waitableServiceTracker = factory.create(WebHookProvider.class, new WaitableServiceTrackerCustomizer<WebHookProvider>()
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
                removeViaValues(registrationsByEvent, registrations);
                removeViaValues(registrationsByKey, registrations);
            }
        });
        this.eventPublisher.register(this);
    }

    private void removeViaValues(Map<?, WebHookRegistration> target,
            Set<WebHookRegistration> registrations)
    {
        for (Iterator i = target.values().iterator(); i.hasNext(); )
        {
            if (registrations.contains(i.next()))
            {
                i.remove();
            }
        }
    }
    
    @EventListener
    public void onEvent(Object event)
    {
        WebHookRegistration reg = registrationsByEvent.get(event.getClass());
        if (reg != null)
        {
            webHookPublisher.publish(reg.getId(), reg.getEventSerializer(event));
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

    public void waitForId(final String webHookId)
    {
        waitableServiceTracker.waitFor(new Predicate<Map<WebHookProvider, WebHookProvider>>()
        {
            @Override
            public boolean apply(Map<WebHookProvider, WebHookProvider> input)
            {
                return registrationsByKey.containsKey(webHookId);
            }
        });
    }
}
