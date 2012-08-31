package com.atlassian.labs.remoteapps.plugin.util.tracker;

import com.atlassian.labs.remoteapps.api.Promise;
import com.atlassian.labs.remoteapps.spi.Promises;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.util.concurrent.AbstractFuture;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.google.common.collect.Maps.*;

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
    private final Class serviceClass;
    private final Function<T, K> extractor;
    private final Set<ServiceFuture<K,T>> futures = new CopyOnWriteArraySet<ServiceFuture<K, T>>();
    private final WaitableServiceTrackerCustomizer<T> customizer;

    public WaitableServiceTracker(final BundleContext bundleContext, Class<T> serviceClass,
            final Function<T, K> extractor,
            final WaitableServiceTrackerCustomizer<T> customizer)
    {
        this.extractor = extractor;
        this.customizer = customizer;
        this.services = newHashMap();

        this.serviceTracker = new ServiceTracker(bundleContext, serviceClass.getName(), new ServiceTrackerCustomizer()
            
        {
            @Override
            public Object addingService(ServiceReference reference)
            {
                T service = (T) bundleContext.getService(reference);
                addIfKeyNotNull(extractor, customizer, service);
                updateFutures();
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
                for (Iterator<T> i = services.values().iterator(); i.hasNext(); )
                {
                    if (service == i.next())
                    {
                        i.remove();
                    }
                }
                customizer.removed((T) service);
                updateFutures();
            }
        });
        this.serviceTracker.open();
        this.serviceClass = serviceClass;
    }

    private void updateFutures()
    {
        // todo: should this be in its own thread/executor?
        for (ServiceFuture<K,T> future : futures)
        {
            if (future.servicesUpdated(services))
            {
                futures.remove(future);
            }
        }
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

    public Promise<Map<K,T>> waitFor(Predicate<Map<K, T>> predicate)
    {
        ServiceFuture<K,T> future = new ServiceFuture<K, T>(predicate);
        if (!future.servicesUpdated(services))
        {
            futures.add(future);
        }
        return Promises.ofFuture(future);
    }

    void close()
    {
        this.serviceTracker.close();
    }
    
    public T get(K key)
    {
        return services.get(key);
    }
    
    public Set<K> getKeys()
    {
        return services.keySet();
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
        for (T service : localServices)
        {
            addIfKeyNotNull(extractor, customizer, service);
        }
        updateFutures();
    }

    private static class ServiceFuture<K, T> extends AbstractFuture<Map<K,T>>
    {
        private Predicate<Map<K, T>> condition;

        private ServiceFuture(Predicate<Map<K, T>> condition)
        {
            this.condition = condition;
        }

        public boolean servicesUpdated(Map<K, T> services)
        {
            if (condition.apply(services))
            {
                set(services);
                return true;
            }
            return false;
        }
    }
}
