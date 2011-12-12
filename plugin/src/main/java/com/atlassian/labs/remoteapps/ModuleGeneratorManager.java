package com.atlassian.labs.remoteapps;

import com.atlassian.labs.remoteapps.descriptor.external.RemoteModuleDescriptor;
import com.atlassian.labs.remoteapps.modules.applinks.ApplicationTypeModuleGenerator;
import com.atlassian.labs.remoteapps.modules.external.RemoteModuleGenerator;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.tracker.DefaultPluginModuleTracker;
import com.atlassian.plugin.tracker.PluginModuleTracker;
import com.atlassian.plugin.util.concurrent.CopyOnWriteMap;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singleton;

/**
 *
 */
@Component
public class ModuleGeneratorManager implements DisposableBean
{
    private final static Logger log = LoggerFactory.getLogger(ModuleGeneratorManager.class);
    private final PluginModuleTracker<RemoteModuleGenerator, RemoteModuleDescriptor> moduleTracker;
    private final ApplicationTypeModuleGenerator applicationTypeModuleGenerator;
    private final Map<String,RemoteModuleGenerator> generatorsByKey = CopyOnWriteMap.newHashMap();

    @Autowired
    public ModuleGeneratorManager(ApplicationTypeModuleGenerator applicationTypeModuleGenerator, PluginAccessor pluginAccessor,
                                  PluginEventManager pluginEventManager)
    {
        this.moduleTracker = new DefaultPluginModuleTracker<RemoteModuleGenerator, RemoteModuleDescriptor>(
                pluginAccessor, pluginEventManager, RemoteModuleDescriptor.class, new PluginModuleTracker.Customizer<RemoteModuleGenerator, RemoteModuleDescriptor>()
        {
            @Override
            public RemoteModuleDescriptor adding(RemoteModuleDescriptor descriptor)
            {
                final RemoteModuleGenerator module = descriptor.getModule();
                generatorsByKey.put(module.getType(), module);
                synchronized(generatorsByKey)
                {
                    generatorsByKey.notifyAll();
                }
                return descriptor;
            }

            @Override
            public void removed(RemoteModuleDescriptor descriptor)
            {
                generatorsByKey.remove(descriptor.getModule().getType());
            }
        });
        this.applicationTypeModuleGenerator = applicationTypeModuleGenerator;
    }

    public void processDescriptor(Element root, ModuleHandler handler)
    {
        waitForModules(root);

        for (Element e : ((Collection<Element>)root.elements()))
        {
            String key = e.getName();
            RemoteModuleGenerator generator = generatorsByKey.get(key);
            handler.handle(e, generator);
        }
    }

    private void waitForModules(Element root)
    {
        Set<String> keys = newHashSet();
        for (Element e : ((Collection<Element>)root.elements()))
        {
            keys.add(e.getName());
        }

        try
        {
            long timeout = System.currentTimeMillis() + 20 * 1000;
            synchronized(generatorsByKey)
            {
                while (!generatorsByKey.keySet().containsAll(keys))
                {
                    generatorsByKey.wait(20 * 1000);
                    if (System.currentTimeMillis() > timeout)
                    {
                        throw new InterruptedException();
                    }
                }
            }
        }
        catch (InterruptedException e)
        {
            keys.removeAll(generatorsByKey.keySet());
            throw new PluginParseException("Unable to locate all modules: " + keys);
        }
    }

    public Iterable<RemoteModuleGenerator> getAllGenerators(Element root)
    {
        waitForModules(root);
        return concat(generatorsByKey.values(), singleton(applicationTypeModuleGenerator));
    }

    public ApplicationTypeModuleGenerator getApplicationTypeModuleGenerator()
    {
        return applicationTypeModuleGenerator;
    }

    public Iterable<RemoteModuleDescriptor> getDescriptors()
    {
        return moduleTracker.getModuleDescriptors();
    }

    @Override
    public void destroy() throws Exception
    {
        moduleTracker.close();
    }

    public static interface ModuleHandler
    {
        public void handle(Element element, RemoteModuleGenerator generator);
    }

}
