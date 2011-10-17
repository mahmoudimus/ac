package com.atlassian.labs.remoteapps.descriptor;

import com.atlassian.labs.remoteapps.modules.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.RemoteModule;
import com.atlassian.labs.remoteapps.modules.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.modules.applinks.ApplicationTypeModule;
import com.atlassian.labs.remoteapps.modules.applinks.ApplicationTypeModuleGenerator;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.descriptors.ChainModuleDescriptorFactory;
import com.google.common.collect.Lists;
import org.dom4j.Element;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.collections.MapUtils.unmodifiableMap;
import static org.apache.commons.lang.Validate.notNull;

/**
 *
 */
public class GeneratorInitializer
{
    private final Set<String> expected;
    private final Set<ModuleDescriptorFactory> factories = new CopyOnWriteArraySet<ModuleDescriptorFactory>();
    private final ApplicationTypeModuleGenerator applicationTypeModuleGenerator;
    private final Map<String,RemoteModuleGenerator> generators;
    private final Plugin plugin;
    private final Bundle bundle;
    private final Element element;

    private final Set<RemoteModule> remoteModules = new CopyOnWriteArraySet<RemoteModule>();
    private final List<ServiceRegistration> serviceRegistrations = new CopyOnWriteArrayList<ServiceRegistration>();

    public GeneratorInitializer(Plugin plugin, Bundle bundle, Iterable<RemoteModuleGenerator> generatorList, Element element)
    {
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
        generators = unmodifiableMap(map);
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
        if (expected.isEmpty() && remoteModules.isEmpty())
        {
            ModuleDescriptorFactory aggFactory = new ChainModuleDescriptorFactory(factories.toArray(new ModuleDescriptorFactory[factories.size()]));

            RemoteAppCreationContext ctx = new RemoteAppCreationContext(plugin, aggFactory, bundle, null);
            ApplicationTypeModule module = (ApplicationTypeModule) applicationTypeModuleGenerator.generate(ctx, element);
            remoteModules.add(module);
            ctx = new RemoteAppCreationContext(plugin, aggFactory, bundle, module.getApplicationType());

            for (Element e : ((Collection<Element>)element.elements()))
            {
                String type = e.getName();
                if (generators.containsKey(type))
                {
                    RemoteModuleGenerator generator = generators.get(type);
                    remoteModules.add(generator.generate(ctx, e));
                }
            }
            registerDescriptors(remoteModules);
        }
        return added;
    }

    private void registerDescriptors(Set<RemoteModule> modules)
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
            module.close();
        }
        remoteModules.clear();
    }

}
