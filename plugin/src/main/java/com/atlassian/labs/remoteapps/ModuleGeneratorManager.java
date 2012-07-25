package com.atlassian.labs.remoteapps;

import com.atlassian.labs.remoteapps.modules.applinks.ApplicationTypeModuleGenerator;
import com.atlassian.labs.remoteapps.modules.external.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.modules.external.WaitableRemoteModuleGenerator;
import com.atlassian.labs.remoteapps.util.tracker.WaitableServiceTracker;
import com.atlassian.labs.remoteapps.util.tracker.WaitableServiceTrackerFactory;
import com.atlassian.plugin.PluginParseException;
import com.google.common.base.Function;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singleton;

/**
 *
 */
@Component
public class ModuleGeneratorManager
{
    private final WaitableServiceTracker<String,RemoteModuleGenerator> moduleTracker;
    private final ApplicationTypeModuleGenerator applicationTypeModuleGenerator;
    private static final Logger log = LoggerFactory.getLogger(ModuleGeneratorManager.class);

    @Autowired
    public ModuleGeneratorManager(ApplicationTypeModuleGenerator applicationTypeModuleGenerator,
            WaitableServiceTrackerFactory waitableServiceTrackerFactory)
    {
        this.moduleTracker = waitableServiceTrackerFactory.create(RemoteModuleGenerator.class,
                new Function<RemoteModuleGenerator,String>() {

                    @Override
                    public String apply(RemoteModuleGenerator from)
                    {
                        return from.getType();
                    }
                });
        this.applicationTypeModuleGenerator = applicationTypeModuleGenerator;
    }

    public void processDescriptor(Element root, ModuleHandler handler)
    {
        for (Element e : ((Collection<Element>)root.elements()))
        {
            String key = e.getName();
            RemoteModuleGenerator generator = moduleTracker.get(key);
            handler.handle(e, generator);
        }
    }

    public void waitForModules(Element root)
    {
        Set<String> keys = newHashSet();
        for (Element e : ((Collection<Element>)root.elements()))
        {
            keys.add(e.getName());
        }

        try
        {
            moduleTracker.waitForKeys(keys).get(20, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            // ignore
        }
        catch (ExecutionException e)
        {
            log.error("Unable to wait for keys", e.getCause());
            throw new RuntimeException(e);
        }
        catch (TimeoutException e)
        {
            throw new PluginParseException("Unable to find all required keys: " + keys);
        }
        processDescriptor(root, new ModuleHandler()
        {
            @Override
            public void handle(Element element, RemoteModuleGenerator generator)
            {
                if (generator instanceof WaitableRemoteModuleGenerator)
                {
                    ((WaitableRemoteModuleGenerator)generator).waitToLoad(element);
                }
            }
        });
    }

    public Iterable<RemoteModuleGenerator> getAllValidatableGenerators()
    {
        return moduleTracker.getAll();
    }
    
    public Iterable<RemoteModuleGenerator> getRemoteModuleGenerators()
    {
        return concat(moduleTracker.getAll(), singleton(applicationTypeModuleGenerator));
    }
    
    public Set<String> getModuleGeneratorKeys()
    {
        return moduleTracker.getKeys();
    }

    public ApplicationTypeModuleGenerator getApplicationTypeModuleGenerator()
    {
        return applicationTypeModuleGenerator;
    }

    public static interface ModuleHandler
    {
        public void handle(Element element, RemoteModuleGenerator generator);
    }

}
