package com.atlassian.labs.remoteapps.util.tracker;

import com.atlassian.plugin.PluginParseException;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Service tracker that aggregates locally-defined services in the application context as well as
 * external osgi service implementations.  The other key function is the ability to wait until key
 * services have been tracked.
 */
public class WaitableServiceTracker<K, T>
{
    private final Map<K, T> services;
    private final ServiceTracker serviceTracker;
    private static final Logger log = LoggerFactory.getLogger(WaitableServiceTracker.class);
    private static final Object waitLock = new Object();
    private final Class serviceClass;
    private final Function<T, K> extractor;
    private final WaitableServiceTrackerCustomizer<T> customizer;

    public WaitableServiceTracker(final BundleContext bundleContext, Class<T> serviceClass,
            final Function<T, K> extractor,
            final WaitableServiceTrackerCustomizer<T> customizer)
    {
        this.extractor = extractor;
        this.customizer = customizer;
        this.services = new ConcurrentHashMap<K, T>();

        this.serviceTracker = new ServiceTracker(bundleContext, serviceClass.getName(), new ServiceTrackerCustomizer()
            
        {
            @Override
            public Object addingService(ServiceReference reference)
            {
                T service = (T) bundleContext.getService(reference);
                synchronized(waitLock)
                {
                    addIfKeyNotNull(extractor, customizer, service);
                    waitLock.notifyAll();
                }
                return service;
            }

            @Override
            public void modifiedService(ServiceReference reference, Object service)
            {
                removedService(reference, service);
                addingService(reference);
            }

            @Override
            public void removedService(ServiceReference reference, Object service)
            {
                services.remove((T) service);
                customizer.removed((T) service);
            }
        });
        this.serviceTracker.open();
        this.serviceClass = serviceClass;
    }

    private void addIfKeyNotNull(Function<T, K> extractor,
            WaitableServiceTrackerCustomizer<T> customizer, T service)
    {
        K key = extractor.apply(service);
        if (key != null)
        {
            this.services.put(key, customizer.adding(service));
        }
    }

    public void waitForKeys(final Collection<K> keys)
    {
        waitFor(new Predicate<Map<K, T>>()
        {
            @Override
            public boolean apply(Map<K, T> services)
            {
                if (log.isDebugEnabled())
                {
                    log.debug(toString());
                }
                return services.keySet().containsAll(keys);
            }

            @Override
            public String toString()
            {
                Set<K> missingKeys = newHashSet(keys);
                missingKeys.removeAll(services.keySet());
                return "Waiting for services: " + missingKeys;
            }
        });
        
    }
    
    public void waitFor(Predicate<Map<K, T>> predicate)
    {
        try
        {
            long timeout = System.currentTimeMillis() + 20 * 1000;
            synchronized(waitLock)
            {
                while (!predicate.apply(services))
                {
                    waitLock.wait(20 * 1000);
                    if (System.currentTimeMillis() > timeout)
                    {
                        throw new InterruptedException();
                    }
                }
            }
        }
        catch (InterruptedException e)
        {
            throw new PluginParseException("Unable to locate all services: " + predicate.toString());
        }
    }

    void close()
    {
        this.serviceTracker.close();
    }
    
    public T get(K key)
    {
        return services.get(key);
    }
    
    public Iterable<T> getAll()
    {
        return services.values();
    }

    Class getServiceClass()
    {
        return serviceClass;
    }

    void loadLocalServices(Collection<T> localServices)
    {
        synchronized(waitLock)
        {
            for (T service : localServices)
            {
                addIfKeyNotNull(extractor, customizer, service);
            }
        }
    }
}
