package com.atlassian.plugin.connect.plugin.web;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.tracker.DefaultPluginModuleTracker;
import com.atlassian.plugin.tracker.PluginModuleTracker;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nonnull;
import javax.servlet.Filter;

@Component
public class DefaultConnectRequestFilterFactory implements ConnectRequestFilterFactory
{
    private final ResettableLazyReference<Multimap<ConnectRequestFilterPhase, ConnectRequestFilterModuleDescriptor>> filters;
    private final DefaultPluginModuleTracker<Filter, ConnectRequestFilterModuleDescriptor> tracker;

    @Autowired
    public DefaultConnectRequestFilterFactory(final PluginAccessor pluginAccessor, final PluginEventManager pluginEventManager)
    {
        PluginModuleTracker.Customizer<Filter, ConnectRequestFilterModuleDescriptor> pluginModuleTrackerCustomizer = new PluginModuleTracker.Customizer<Filter, ConnectRequestFilterModuleDescriptor>()
        {
            @Override
            public ConnectRequestFilterModuleDescriptor adding(final ConnectRequestFilterModuleDescriptor descriptor)
            {
                filters.reset();
                return descriptor;
            }

            @Override
            public void removed(final ConnectRequestFilterModuleDescriptor descriptor)
            {
                filters.reset();
            }
        };
        this.tracker = new DefaultPluginModuleTracker<Filter, ConnectRequestFilterModuleDescriptor>(pluginAccessor, pluginEventManager, ConnectRequestFilterModuleDescriptor.class, pluginModuleTrackerCustomizer);

        this.filters = new ResettableLazyReference<Multimap<ConnectRequestFilterPhase, ConnectRequestFilterModuleDescriptor>>()
        {
            @Override
            protected Multimap<ConnectRequestFilterPhase, ConnectRequestFilterModuleDescriptor> create() throws Exception
            {
                final LinkedHashMultimap<ConnectRequestFilterPhase, ConnectRequestFilterModuleDescriptor> multimap = LinkedHashMultimap.create();
                final Iterable<ConnectRequestFilterModuleDescriptor> moduleDescriptors = tracker.getModuleDescriptors();

                for (ConnectRequestFilterModuleDescriptor descriptor : moduleDescriptors)
                {
                    multimap.put(descriptor.getFilterPhase(), descriptor);
                }

                return multimap;
            }
        };
    }

    @Override
    @Nonnull
    public Iterable<Filter> getFiltersForPhase(final ConnectRequestFilterPhase phase)
    {
        final Collection<ConnectRequestFilterModuleDescriptor> descriptors = filters.get().get(phase);
        if (!Iterables.isEmpty(descriptors))
        {
            return Iterables.transform(descriptors, new Function<ConnectRequestFilterModuleDescriptor, Filter>()
            {
                @Override
                public Filter apply(final ConnectRequestFilterModuleDescriptor descriptor)
                {
                    return descriptor.getModule();
                }
            });
        }
        else
        {
            return Collections.emptyList();
        }
    }

    @VisibleForTesting
    public DefaultPluginModuleTracker<Filter, ConnectRequestFilterModuleDescriptor> getTracker()
    {
        return tracker;
    }
}
