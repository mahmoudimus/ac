package com.atlassian.plugin.remotable.plugin.integration.plugins;

import com.atlassian.plugin.remotable.host.common.util.BundleUtil;
import com.atlassian.osgi.tracker.WaitableServiceTracker;
import com.atlassian.osgi.tracker.WaitableServiceTrackerFactory;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.Effect;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.util.concurrent.FutureCallback;
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

import static com.google.common.collect.Maps.*;
import static com.google.common.collect.Sets.*;
import static java.util.Arrays.*;

/**
 * Helper component that registers dynamic module descriptors
 */
@Component
public class DynamicDescriptorRegistration
{
    private final WaitableServiceTracker<ModuleDescriptorFactory,ModuleDescriptorFactory> moduleTracker;
    private final BundleContext bundleContext;
    private final I18nPropertiesPluginManager i18nPropertiesPluginManager;
    private static final Logger log = LoggerFactory.getLogger(DynamicDescriptorRegistration.class);

    @Autowired
    public DynamicDescriptorRegistration(WaitableServiceTrackerFactory waitableServiceTrackerFactory,
                                         BundleContext bundleContext,
                                         I18nPropertiesPluginManager i18nPropertiesPluginManager
    )
    {
        this.bundleContext = bundleContext;
        this.i18nPropertiesPluginManager = i18nPropertiesPluginManager;
        this.moduleTracker = waitableServiceTrackerFactory.create(ModuleDescriptorFactory.class);
    }

    public void onKeys(final FutureCallback<Map<String, ModuleDescriptorFactory>> callback, final String... requiredKeys)
    {
        moduleTracker.waitFor(
                new Predicate<Map<ModuleDescriptorFactory, ModuleDescriptorFactory>>()
                {
                    @Override
                    public boolean apply(Map<ModuleDescriptorFactory, ModuleDescriptorFactory> factories)
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
                }).done(new Effect<Map<ModuleDescriptorFactory, ModuleDescriptorFactory>>()
                    {
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

                        @Override
                        public void apply(Map<ModuleDescriptorFactory, ModuleDescriptorFactory> value)
                        {
                            callback.onSuccess(factoriesToMap(value.keySet()));
                        }
                    })
                .fail(new Effect<Throwable>()
                {
                    @Override
                    public void apply(Throwable value)
                    {
                        callback.onFailure(value);
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

    public void registerDescriptors(Plugin plugin, DescriptorToRegister... descriptors)
    {
        registerDescriptors(plugin, asList(descriptors));
    }

    public void registerDescriptors(Plugin plugin, Iterable<DescriptorToRegister> descriptors)
    {
        Bundle bundle = BundleUtil.findBundleForPlugin(bundleContext, plugin.getKey());
        BundleContext targetBundleContext = bundle.getBundleContext();
        for (DescriptorToRegister reg : descriptors)
        {
            ModuleDescriptor descriptor = reg.getDescriptor();
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

            if (reg.getI18nProperties() != null)
            {
                i18nPropertiesPluginManager.add(plugin.getKey(), reg.getI18nProperties());
            }
        }
    }
}
