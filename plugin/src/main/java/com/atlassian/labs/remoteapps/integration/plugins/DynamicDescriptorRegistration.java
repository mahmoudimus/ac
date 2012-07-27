package com.atlassian.labs.remoteapps.integration.plugins;

import com.atlassian.labs.remoteapps.modules.applinks.ApplicationTypeModuleGenerator;
import com.atlassian.labs.remoteapps.modules.external.RemoteModule;
import com.atlassian.labs.remoteapps.modules.external.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.util.BundleUtil;
import com.atlassian.labs.remoteapps.util.tracker.WaitableServiceTracker;
import com.atlassian.labs.remoteapps.util.tracker.WaitableServiceTrackerFactory;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.external.ListableModuleDescriptorFactory;
import com.atlassian.plugin.osgi.external.SingleModuleDescriptorFactory;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.netbeans.lib.cvsclient.commandLine.command.log;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;

/**
 * Created with IntelliJ IDEA. User: mrdon Date: 7/24/12 Time: 5:26 PM To change this template use
 * File | Settings | File Templates.
 */
@Component
public class DynamicDescriptorRegistration
{
    private final WaitableServiceTracker<ModuleDescriptorFactory,ModuleDescriptorFactory> moduleTracker;
    private final BundleContext bundleContext;
    private static final Logger log = LoggerFactory.getLogger(DynamicDescriptorRegistration.class);

    @Autowired
    public DynamicDescriptorRegistration(
            WaitableServiceTrackerFactory waitableServiceTrackerFactory,
            BundleContext bundleContext)
    {
        this.bundleContext = bundleContext;
        this.moduleTracker = waitableServiceTrackerFactory.create(ModuleDescriptorFactory.class,
                new Function<ModuleDescriptorFactory,ModuleDescriptorFactory>() {

                    @Override
                    public ModuleDescriptorFactory apply(ModuleDescriptorFactory from)
                    {
                        return from;
                    }
                });
    }

    public void onKeys(
            final FutureCallback<Map<String, ModuleDescriptorFactory>> callback, final String... requiredKeys)
    {
        Futures.addCallback(moduleTracker.waitFor(
                new Predicate<Map<ModuleDescriptorFactory, ModuleDescriptorFactory>>()
                {
                    @Override
                    public boolean apply(
                            Map<ModuleDescriptorFactory, ModuleDescriptorFactory> factories)
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
                }),
                new FutureCallback<Map<ModuleDescriptorFactory, ModuleDescriptorFactory>>()
                {
                    @Override
                    public void onSuccess(Map<ModuleDescriptorFactory, ModuleDescriptorFactory> result)
                    {
                        callback.onSuccess(factoriesToMap(result.keySet()));
                    }

                    @Override
                    public void onFailure(Throwable t)
                    {
                        callback.onFailure(t);
                    }

                    Map<String,ModuleDescriptorFactory> factoriesToMap(Iterable<ModuleDescriptorFactory> factories)
                    {
                        Map<String, ModuleDescriptorFactory> result = newHashMap();
                        for (String key : requiredKeys)
                        {
                            for (ModuleDescriptorFactory factory : factories)
                            {
                                if (factory.hasModuleDescriptor(key))
                                {
                                    result.put(key, factory);
                                    break;
                                }
                            }
                        }
                        return result;
                    }
                });
    }

    public <M, D extends ModuleDescriptor<M>> void createDynamicModuleDescriptor(final String key,
            final M moduleInstance, final Function<D, Void> callback)
    {
        onKeys(new FutureCallback<Map<String, ModuleDescriptorFactory>>()
        {
            @Override
            public void onSuccess(Map<String, ModuleDescriptorFactory> result)
            {
                try
                {
                    Class<D> descriptorClass = (Class<D>) result.get(key).getModuleDescriptorClass(key);
                    D descriptor = null;
                    try
                    {
                        descriptor = (D) descriptorClass.getConstructor(ModuleFactory.class)
                                .newInstance(
                                        new ModuleFactory()
                                        {
                                            @Override
                                            public <T> T createModule(String s,
                                                    ModuleDescriptor<T> tModuleDescriptor) throws
                                                    PluginParseException
                                            {
                                                return (T) moduleInstance;
                                            }
                                        });
                        callback.apply(descriptor);
                    }
                    catch (InstantiationException e)
                    {
                        throw new PluginParseException(e);
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new PluginParseException(e);
                    }
                    catch (InvocationTargetException e)
                    {
                        throw new PluginParseException(e);
                    }
                    catch (NoSuchMethodException e)
                    {
                        throw new PluginParseException(e);
                    }
                }
                catch (PluginParseException e)
                {
                    throw e;
                }
                catch (Exception e)
                {
                    throw new PluginParseException(e);
                }
            }

            @Override
            public void onFailure(Throwable t)
            {
                throw new RuntimeException(t);
            }
        }, key);
    }

    public void registerDescriptors(Plugin plugin, ModuleDescriptor... descriptors)
    {
        registerDescriptors(plugin, asList(descriptors));
    }

    public void registerDescriptors(Plugin plugin, Iterable<ModuleDescriptor> descriptors)
    {
        Bundle bundle = BundleUtil.findBundleForPlugin(bundleContext, plugin.getKey());
        BundleContext targetBundleContext = bundle.getBundleContext();
        for (ModuleDescriptor descriptor : descriptors)
        {
            if (plugin.getModuleDescriptor(descriptor.getKey()) != null)
            {
                log.error("Duplicate key '" + descriptor.getKey() + "' detected, skipping");
            }
            else
            {
                log.debug("Registering descriptor {}", descriptor.getClass().getName());
                targetBundleContext.registerService(ModuleDescriptor.class.getName(),
                        descriptor, null);
            }
        }
    }

}
