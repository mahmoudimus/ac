package com.atlassian.plugin.connect.core.descriptor;

import com.atlassian.osgi.tracker.WaitableServiceTracker;
import com.atlassian.osgi.tracker.WaitableServiceTrackerFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ClasspathComponent;
import com.atlassian.plugin.schema.descriptor.DescribedModuleDescriptorFactory;

import com.google.common.base.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public final class OsgiDescribedModuleDescriptorFactoryAccessor implements DescribedModuleDescriptorFactoryAccessor
{
    private final WaitableServiceTracker<String, DescribedModuleDescriptorFactory> serviceTracker;

    @Autowired
    public OsgiDescribedModuleDescriptorFactoryAccessor(@ClasspathComponent WaitableServiceTrackerFactory waitableServiceTrackerFactory)
    {
        Function<DescribedModuleDescriptorFactory, String> f = identityHashCode();
        serviceTracker = checkNotNull(waitableServiceTrackerFactory).create(DescribedModuleDescriptorFactory.class, f);
    }

    @Override
    public Iterable<DescribedModuleDescriptorFactory> getDescribedModuleDescriptorFactories()
    {
        return serviceTracker.getAll();
    }

    private static <T> Function<T, String> identityHashCode()
    {
        return new IdentityHashCodeFunction<T>();
    }

    private static final class IdentityHashCodeFunction<T> implements Function<T, String>
    {
        @Override
        public String apply(T t)
        {
            return String.valueOf(System.identityHashCode(t));
        }
    }
}
