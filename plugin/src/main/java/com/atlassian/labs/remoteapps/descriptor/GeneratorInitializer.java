package com.atlassian.labs.remoteapps.descriptor;

import com.atlassian.labs.remoteapps.AccessLevelManager;
import com.atlassian.labs.remoteapps.descriptor.external.AccessLevel;
import com.atlassian.labs.remoteapps.modules.*;
import com.atlassian.labs.remoteapps.modules.applinks.ApplicationTypeModule;
import com.atlassian.labs.remoteapps.modules.applinks.ApplicationTypeModuleGenerator;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.descriptors.ChainModuleDescriptorFactory;
import org.dom4j.Element;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Manages RemoteModuleGenerators
 */
class GeneratorInitializer
{
    private final Set<String> expected;
    private final Set<ModuleDescriptorFactory> factories = new CopyOnWriteArraySet<ModuleDescriptorFactory>();
    private final ApplicationTypeModuleGenerator applicationTypeModuleGenerator;
    private final Map<String,RemoteModuleGenerator> generators;
    private final AccessLevelManager accessLevelManager;
    private final StartableForPlugins startableForPlugins;
    private final Plugin plugin;
    private final Bundle bundle;
    private final Element element;
    private static final Logger log = LoggerFactory.getLogger(GeneratorInitializer.class);

    private final Collection<RemoteModule> remoteModules = new CopyOnWriteArrayList<RemoteModule>();
    private final List<ServiceRegistration> serviceRegistrations = new CopyOnWriteArrayList<ServiceRegistration>();

    GeneratorInitializer(AccessLevelManager accessLevelManager, StartableForPlugins startableForPlugins, Plugin plugin, Bundle bundle, Iterable<RemoteModuleGenerator> generatorList, Element element)
    {
        this.accessLevelManager = accessLevelManager;
        this.startableForPlugins = startableForPlugins;
        this.plugin = plugin;
        this.bundle = bundle;
        this.element = element;
        Set<String> set = newHashSet();
        ApplicationTypeModuleGenerator appTypeGen = null;
        Map<String,RemoteModuleGenerator> map = newHashMap();
        for (RemoteModuleGenerator generator : generatorList)
        {
            set.addAll(generator.getDynamicModuleTypeDependencies());
            if (generator instanceof ApplicationTypeModuleGenerator)
            {
                appTypeGen = (ApplicationTypeModuleGenerator) generator;
            }
            else
            {
                map.put(generator.getType(), generator);
            }
        }
        generators = Collections.unmodifiableMap(map);
        notNull(appTypeGen);
        applicationTypeModuleGenerator = appTypeGen;
        this.expected = set;
    }

    public boolean registerNewModuleDescriptorFactory(ModuleDescriptorFactory factory)
    {
        boolean added = false;
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

    public void init(String accessLevelValue)
    {
        if (expected.isEmpty() && remoteModules.isEmpty())
        {
            ModuleDescriptorFactory aggFactory = new ChainModuleDescriptorFactory(factories.toArray(new ModuleDescriptorFactory[factories.size()]));

            AccessLevel accessLevel = accessLevelManager.getAccessLevel(accessLevelValue);
            RemoteAppCreationContext ctx = new RemoteAppCreationContext(plugin, aggFactory, bundle, null, accessLevel);
            ApplicationTypeModule module = (ApplicationTypeModule) applicationTypeModuleGenerator.generate(ctx, element);
            remoteModules.add(module);
            ctx = new RemoteAppCreationContext(plugin, aggFactory, bundle, module.getApplicationType(), accessLevel);

            for (Element e : ((Collection<Element>)element.elements()))
            {
                String type = e.getName();
                if (generators.containsKey(type))
                {
                    RemoteModuleGenerator generator = generators.get(type);
                    final RemoteModule remoteModule = generator.generate(ctx, e);
                    remoteModules.add(remoteModule);
                }
                else
                {
                    log.warn("Unknown module: " + type);
                }
            }

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

    private void registerDescriptors(Iterable<RemoteModule> modules)
    {
        BundleContext targetBundleContext = bundle.getBundleContext();
        for (RemoteModule module : modules)
        {
            for (ModuleDescriptor descriptor : module.getModuleDescriptors())
            {
                serviceRegistrations.add(targetBundleContext.registerService(ModuleDescriptor.class.getName(), descriptor, null));
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
