package com.atlassian.labs.remoteapps.descriptor;

import com.atlassian.labs.remoteapps.modules.RemoteModuleGenerator;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.module.LegacyModuleFactory;
import com.atlassian.util.concurrent.NotNull;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Element;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.atlassian.labs.remoteapps.util.BundleUtil.findBundleForPlugin;
import static com.google.common.collect.Sets.newHashSet;

/**
 *
 */
public class RemoteAppModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final BundleContext bundleContext;
    private final ApplicationContext applicationContext;
    private final StartableForPlugins startableForPlugins;

    private ServiceTracker serviceTracker;
    private Element originalElement;
    private Iterable<RemoteModuleGenerator> generators;

    public RemoteAppModuleDescriptor(BundleContext bundleContext, ApplicationContext applicationContext, StartableForPlugins startableForPlugins)
    {
        super(new LegacyModuleFactory());
        this.bundleContext = bundleContext;
        this.applicationContext = applicationContext;
        this.startableForPlugins = startableForPlugins;
    }

    @Override
    public void init(@NotNull Plugin plugin, @NotNull Element element) throws PluginParseException
    {
        super.init(plugin, element);
        this.originalElement = element;
        this.generators = Collections.unmodifiableCollection((Collection<RemoteModuleGenerator>) applicationContext.getBeansOfType(RemoteModuleGenerator.class).values());
    }

    @Override
    public void enabled()
    {
        super.enabled();
        if (serviceTracker == null)
        {
            // generate and register new services
            Bundle targetBundle = findBundleForPlugin(bundleContext, getPluginKey());
            final BundleContext targetBundleContext = targetBundle.getBundleContext();
            final GeneratorInitializer generatorInitializer = new GeneratorInitializer(startableForPlugins, getPlugin(), targetBundle, generators, originalElement);
            this.serviceTracker = new ServiceTracker(targetBundleContext, ModuleDescriptorFactory.class.getName(), new ServiceTrackerCustomizer()
            {
                private Map<String,ModuleDescriptorFactory> factories;

                @Override
                public Object addingService(ServiceReference reference)
                {
                    Object svc = targetBundleContext.getService(reference);
                    ModuleDescriptorFactory factory = (ModuleDescriptorFactory) svc;
                    if (generatorInitializer.registerNewModuleDescriptorFactory(factory))
                    {
                        return factory;
                    }
                    else
                    {
                        return null;
                    }
                }

                @Override
                public void modifiedService(ServiceReference reference, Object service)
                {
                }

                @Override
                public void removedService(ServiceReference reference, Object service)
                {
                    generatorInitializer.close();
                    // todo: recover in case a dependent factory is just being reloaded
                }
            });
            serviceTracker.open();
        }
    }

    @Override
    public void disabled()
    {
        super.disabled();
        serviceTracker.close();
        serviceTracker = null;
    }

    @Override
    public Void getModule()
    {
        return null;
    }
}
