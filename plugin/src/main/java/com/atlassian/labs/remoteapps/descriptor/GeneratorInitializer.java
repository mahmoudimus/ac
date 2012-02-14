package com.atlassian.labs.remoteapps.descriptor;

import com.atlassian.event.api.EventListener;
import com.atlassian.labs.remoteapps.ModuleGeneratorManager;
import com.atlassian.labs.remoteapps.event.RemoteAppUninstalledEvent;
import com.atlassian.labs.remoteapps.modules.DefaultRemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.applinks.ApplicationTypeModule;
import com.atlassian.labs.remoteapps.modules.external.*;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.descriptors.ChainModuleDescriptorFactory;
import com.google.common.base.Function;
import org.dom4j.Element;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Manages RemoteModuleGenerators
 */
public class GeneratorInitializer
{
    private final Set<ModuleDescriptorFactory> factories = new CopyOnWriteArraySet<ModuleDescriptorFactory>();
    private final StartableForPlugins startableForPlugins;
    private final Plugin plugin;
    private final Bundle bundle;
    private final ModuleGeneratorManager moduleGeneratorManager;
    private final Element element;
    private static final Logger log = LoggerFactory.getLogger(GeneratorInitializer.class);

    private final Collection<RemoteModule> remoteModules = new CopyOnWriteArrayList<RemoteModule>();
    private final List<ServiceRegistration> serviceRegistrations = new CopyOnWriteArrayList<ServiceRegistration>();

    private volatile Set<String> expected;

    GeneratorInitializer(StartableForPlugins startableForPlugins, Plugin plugin, Bundle bundle,
            ModuleGeneratorManager moduleGeneratorManager, Element element)
    {
        this.startableForPlugins = startableForPlugins;
        this.plugin = plugin;
        this.bundle = bundle;
        this.moduleGeneratorManager = moduleGeneratorManager;
        this.element = element;
    }

    @EventListener
    public void onAppUninstall(RemoteAppUninstalledEvent event)
    {
        if (!plugin.getKey().equals(event.getRemoteAppKey()))
        {
            return;
        }
        for (RemoteModule module : remoteModules)
        {
            if (module instanceof UninstallableRemoteModule)
            {
                ((UninstallableRemoteModule)module).uninstall();
            }
        }
    }

    public boolean registerNewModuleDescriptorFactory(ModuleDescriptorFactory factory)
    {
        boolean added = false;
        ensureExpected();
        for (Iterator<String> i = expected.iterator(); i.hasNext(); )
        {
            if (factory.hasModuleDescriptor(i.next()))
            {
                i.remove();
                factories.add(factory);
                added = true;
                break;
            }
        }
        return added;
    }

    public void init()
    {
        ensureExpected();
        if (expected.isEmpty() && remoteModules.isEmpty())
        {
            ModuleDescriptorFactory aggFactory = new ChainModuleDescriptorFactory(factories.toArray(new ModuleDescriptorFactory[factories.size()]));

            final RemoteAppCreationContext firstContext = new DefaultRemoteAppCreationContext(plugin, aggFactory, bundle, null);
            ApplicationTypeModule module = (ApplicationTypeModule) moduleGeneratorManager.getApplicationTypeModuleGenerator().generate(firstContext, element);
            remoteModules.add(module);
            final RemoteAppCreationContext childContext = new DefaultRemoteAppCreationContext(plugin, aggFactory, bundle, module.getApplicationType());

            moduleGeneratorManager.processDescriptor(element, new ModuleGeneratorManager.ModuleHandler()
            {
                @Override
                public void handle(Element element, RemoteModuleGenerator generator)
                {
                    log.info("Registering module '" + generator.getType() + "' for key '" + element.attributeValue("key") + "'");
                    remoteModules.add(generator.generate(childContext, element));
                }
            });
            registerDescriptors(remoteModules);
            for (final RemoteModule remoteModule : remoteModules)
            {
                if (remoteModule instanceof StartableRemoteModule)
                {
                    startableForPlugins.register(plugin.getKey(), new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            ((StartableRemoteModule)remoteModule).start();
                        }
                    });
                }
            }
        }
        else
        {
            throw new IllegalStateException("All required module types '" + expected + "' not found or this hasn't been shut down properly");
        }
    }

    private void ensureExpected()
    {
        if (expected == null)
        {
            this.expected = newHashSet(concat(transform(moduleGeneratorManager.getAllGenerators(element), new Function<RemoteModuleGenerator,Iterable<String>>()
            {
                @Override
                public Iterable<String> apply(RemoteModuleGenerator from)
                {
                    return from.getDynamicModuleTypeDependencies();
                }
            })));
        }
    }

    private void registerDescriptors(Iterable<RemoteModule> modules)
    {
        BundleContext targetBundleContext = bundle.getBundleContext();
        for (RemoteModule module : modules)
        {
            for (ModuleDescriptor descriptor : module.getModuleDescriptors())
            {
                if (plugin.getModuleDescriptor(descriptor.getKey()) != null)
                {
                    log.error("Duplicate key '" + descriptor.getKey() + "' detected, skipping");
                }
                else
                {
                    serviceRegistrations.add(targetBundleContext.registerService(ModuleDescriptor.class.getName(), descriptor, null));
                }
            }
        }
    }

    public void close()
    {
        for (ServiceRegistration reg : serviceRegistrations)
        {
            try
            {
                reg.unregister();
            }
            catch (IllegalStateException ex)
            {
                // no worries, this only means the bundle was already shut down so the services aren't valid anymore
            }
        }
        serviceRegistrations.clear();
        for (RemoteModule module : remoteModules)
        {
            if (module instanceof ClosableRemoteModule)
            {
                ((ClosableRemoteModule)module).close();
            }
        }
        remoteModules.clear();
        startableForPlugins.unregister(plugin.getKey());
    }
}
