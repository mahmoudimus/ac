package com.atlassian.labs.remoteapps.descriptor;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.AccessLevelManager;
import com.atlassian.labs.remoteapps.ModuleGeneratorManager;
import com.atlassian.labs.remoteapps.event.RemoteAppStartedEvent;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.LegacyModuleFactory;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.labs.remoteapps.util.BundleUtil.findBundleForPlugin;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getOptionalAttribute;
import static com.google.common.collect.Sets.newHashSet;

/**
 * The module descriptor for remote-app
 */
public class RemoteAppModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final BundleContext bundleContext;
    private final StartableForPlugins startableForPlugins;
    private final AccessLevelManager accessLevelManager;
    private final EventPublisher eventPublisher;

    private static final Logger log = LoggerFactory.getLogger(RemoteAppModuleDescriptor.class);

    private ServiceTracker serviceTracker;
    private Element originalElement;
    private String accessLevel;
    private final ModuleGeneratorManager moduleGeneratorManager;

    public RemoteAppModuleDescriptor(BundleContext bundleContext,
                                     StartableForPlugins startableForPlugins,
                                     AccessLevelManager accessLevelManager,
                                     ModuleGeneratorManager moduleGeneratorManager, EventPublisher eventPublisher)
    {
        super(new LegacyModuleFactory());
        this.bundleContext = bundleContext;
        this.startableForPlugins = startableForPlugins;
        this.accessLevelManager = accessLevelManager;
        this.moduleGeneratorManager = moduleGeneratorManager;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void init(@NotNull Plugin plugin, @NotNull Element element) throws PluginParseException
    {
        super.init(plugin, element);
        this.originalElement = element;
        this.accessLevel = getOptionalAttribute(element, "access-level", "global");
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
            final GeneratorInitializer generatorInitializer = new GeneratorInitializer(accessLevelManager, startableForPlugins, getPlugin(), targetBundle, moduleGeneratorManager, originalElement);
            this.serviceTracker = new ServiceTracker(targetBundleContext, ModuleDescriptorFactory.class.getName(), new ServiceTrackerCustomizer()
            {
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
            startableForPlugins.register(getPluginKey(), new Runnable()
            {

                @Override
                public void run()
                {
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            serviceTracker.open();
                            generatorInitializer.init(accessLevel);
                            eventPublisher.publish(new RemoteAppStartedEvent(getPluginKey()));
                            log.info("Remote app '{}' started successfully", getPluginKey());
                        }
                    }).start();
                }
            });
        }
    }

    @Override
    public void disabled()
    {
        super.disabled();
        if (serviceTracker != null)
        {
            serviceTracker.close();
        }
        serviceTracker = null;
    }

    @Override
    public Void getModule()
    {
        return null;
    }
}
