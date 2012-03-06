package com.atlassian.labs.remoteapps.loader;

import com.atlassian.labs.remoteapps.util.tracker.WaitableServiceTracker;
import com.atlassian.labs.remoteapps.util.tracker.WaitableServiceTrackerFactory;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.osgi.external.ListableModuleDescriptorFactory;
import com.atlassian.plugin.osgi.external.SingleModuleDescriptorFactory;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;

/**
 * Aggregates dynamic module descriptors for easy synchronous access
 */
@Component
public class AggregateModuleDescriptorFactory implements ModuleDescriptorFactory
{
    private final WaitableServiceTracker<ModuleDescriptorFactory,ModuleDescriptorFactory> tracker;
    private static final Logger log = LoggerFactory.getLogger(
            AggregateModuleDescriptorFactory.class);
    private static final NoOpModuleDescriptorFactory NULL_FACTORY = new NoOpModuleDescriptorFactory();

    @Autowired
    public AggregateModuleDescriptorFactory(WaitableServiceTrackerFactory waitableServiceTrackerFactory)
    {
        this.tracker = waitableServiceTrackerFactory.create(ModuleDescriptorFactory.class,
                new Function<ModuleDescriptorFactory,ModuleDescriptorFactory>()
                {
                    @Override
                    public ModuleDescriptorFactory apply(@Nullable ModuleDescriptorFactory from)
                    {
                        return from == AggregateModuleDescriptorFactory.this ? null : from;
                    }
                }
            );
    }
    
    public void waitForRequiredDescriptors(String... requiredKeys)
    {
        waitForRequiredDescriptors(asList(requiredKeys));
    }
    public void waitForRequiredDescriptors(final Collection<String> requiredKeys)
    {
        tracker.waitFor(new Predicate<Map<ModuleDescriptorFactory,ModuleDescriptorFactory>>()
        {
            @Override
            public boolean apply(Map<ModuleDescriptorFactory,ModuleDescriptorFactory> factories)
            {
                Set<String> keys = newHashSet(requiredKeys);
                for (Iterator<String> i = keys.iterator(); i.hasNext(); )
                {
                    String key = i.next();
                    for (ModuleDescriptorFactory factory : factories.keySet())
                    {
                        if (factory.hasModuleDescriptor(key))
                        {
                            i.remove();
                            break;
                        }
                    }
                }
                log.info("Waiting on dynamic module types: " + keys);
                
                return keys.isEmpty();
            }

            @Override
            public String toString()
            {
                return "Waiting for module descriptors: " + requiredKeys;
            }
        });
    }
    
    private ModuleDescriptorFactory getTrackedModuleDescriptorFactory(String key)
    {
        for (ModuleDescriptorFactory factory : tracker.getAll())
        {
            if (factory.hasModuleDescriptor(key))
            {
                return factory;
            }
        }
        return NULL_FACTORY;
    }

    @Override
    public ModuleDescriptor<?> getModuleDescriptor(String type) throws PluginParseException,
                                                                       IllegalAccessException,
                                                                       InstantiationException,
                                                                       ClassNotFoundException
    {
        return getTrackedModuleDescriptorFactory(type).getModuleDescriptor(type);
    }

    @Override
    public Class<? extends ModuleDescriptor> getModuleDescriptorClass(String type)
    {
        return getTrackedModuleDescriptorFactory(type).getModuleDescriptorClass(type); 
    }

    @Override
    public boolean hasModuleDescriptor(String type)
    {
        return getTrackedModuleDescriptorFactory(type).hasModuleDescriptor(type);
    }

    private static class NoOpModuleDescriptorFactory implements ModuleDescriptorFactory
    {

        @Override
        public ModuleDescriptor<?> getModuleDescriptor(String type) throws PluginParseException,
                                                                           IllegalAccessException,
                                                                           InstantiationException,
                                                                           ClassNotFoundException
        {
            return null;
        }

        @Override
        public Class<? extends ModuleDescriptor> getModuleDescriptorClass(String type)
        {
            return null;
        }

        @Override
        public boolean hasModuleDescriptor(String type)
        {
            return false;
        }
    }
}
