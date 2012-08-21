package com.atlassian.labs.remoteapps.plugin.util.tracker;

import com.google.common.base.Function;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Creates waitable service trackers.
 */
@Component
public class WaitableServiceTrackerFactory implements DisposableBean, ApplicationListener
{

    private final BundleContext bundleContext;
    private final Set<WaitableServiceTracker<?,?>> serviceTrackers;

    @Autowired
    public WaitableServiceTrackerFactory(BundleContext bundleContext)
    {
        this.bundleContext = bundleContext;
        this.serviceTrackers = new CopyOnWriteArraySet<WaitableServiceTracker<?,?>>();
    }

    public <K, T> WaitableServiceTracker<K,T> create(Class<T> serviceClass,
            Function<T,K> keyExtractor)
    {
        return create(serviceClass, keyExtractor, new NoOpWaitableServiceTrackerCustomizer<T>());
    }
    public <K, T> WaitableServiceTracker<K,T> create(Class<T> serviceClass, 
            Function<T,K> keyExtractor, WaitableServiceTrackerCustomizer<T> customizer)
    {
        WaitableServiceTracker<K,T> tracker = new WaitableServiceTracker<K,T>(bundleContext,
                serviceClass, keyExtractor, customizer);
        serviceTrackers.add(tracker);
        return tracker;
    }

    @Override
    public void destroy() throws Exception
    {
        for (WaitableServiceTracker<?,?> tracker : serviceTrackers)
        {
            tracker.close();
        }
    }

    public <T> WaitableServiceTracker<T, T> create(Class<T> serviceClass)
    {
        return create(serviceClass, new ValueAsKeyFunction<T>());
    }

    public <T> WaitableServiceTracker<T, T> create(Class<T> serviceClass, WaitableServiceTrackerCustomizer<T> customizer)
    {
        return create(serviceClass, new ValueAsKeyFunction<T>(), customizer);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event)
    {
        if (event instanceof ContextRefreshedEvent)
        {
            ApplicationContext ctx = ((ContextRefreshedEvent)event).getApplicationContext();
            for (WaitableServiceTracker tracker : serviceTrackers)
            {
                tracker.loadLocalServices(ctx.getBeansOfType(tracker.getServiceClass()).values());
            }
        }
    }

    private static class NoOpWaitableServiceTrackerCustomizer<T> implements WaitableServiceTrackerCustomizer<T>
    {

        @Override
        public T adding(T service)
        {
            return service;
        }

        @Override
        public void removed(T service)
        {
        }
    }

    private static class ValueAsKeyFunction<T> implements Function<T, T>
    {
        @Override
        public T apply(T from)
        {
            return from;
        }
    }
}
