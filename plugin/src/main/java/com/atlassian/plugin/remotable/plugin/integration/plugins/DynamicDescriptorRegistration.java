package com.atlassian.plugin.remotable.plugin.integration.plugins;

import com.atlassian.plugin.*;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.descriptors.UnrecognisedModuleDescriptor;
import com.atlassian.plugin.remotable.host.common.util.BundleUtil;
import com.atlassian.osgi.tracker.WaitableServiceTracker;
import com.atlassian.osgi.tracker.WaitableServiceTrackerFactory;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.Effect;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.util.concurrent.FutureCallback;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.*;
import static com.google.common.collect.Sets.*;
import static java.util.Arrays.*;

/**
 * Helper component that registers dynamic module descriptors
 */
@Component
public class DynamicDescriptorRegistration
{
    private final BundleContext bundleContext;
    private final I18nPropertiesPluginManager i18nPropertiesPluginManager;
    private static final Logger log = LoggerFactory.getLogger(DynamicDescriptorRegistration.class);

    public static interface Registration
    {
        void unregister();
    }

    @Autowired
    public DynamicDescriptorRegistration(
                                         BundleContext bundleContext,
                                         I18nPropertiesPluginManager i18nPropertiesPluginManager
    )
    {
        this.bundleContext = bundleContext;
        this.i18nPropertiesPluginManager = i18nPropertiesPluginManager;
    }

    /**
     * Registers descriptors.  <strong>Important: make sure any osgi service references passed into
     * these descriptors are not proxies created by the p3 plugin, as it will cause ServiceProxyDestroyed
     * exceptions when the p3 plugin is upgraded.</strong>
     *
     * @param plugin
     * @param descriptors
     * @return
     */
    public Registration registerDescriptors(Plugin plugin, DescriptorToRegister... descriptors)
    {
        return registerDescriptors(plugin, asList(descriptors));
    }

    /**
     * Registers descriptors.  <strong>Important: make sure any osgi service references passed into
     * these descriptors are not proxies created by the p3 plugin, as it will cause ServiceProxyDestroyed
     * exceptions when the p3 plugin is upgraded.</strong>
     *
     * @param plugin
     * @param descriptors
     * @return
     */
    public Registration registerDescriptors(final Plugin plugin, Iterable<DescriptorToRegister> descriptors)
    {
        Bundle bundle = BundleUtil.findBundleForPlugin(bundleContext, plugin.getKey());
        BundleContext targetBundleContext = bundle.getBundleContext();
        final List<ServiceRegistration> registrations = newArrayList();
        for (DescriptorToRegister reg : descriptors)
        {
            ModuleDescriptor descriptor = reg.getDescriptor();
            ModuleDescriptor<?> existingDescriptor = plugin.getModuleDescriptor(descriptor.getKey());
            if (existingDescriptor != null)
            {
                log.error("Duplicate key '" + descriptor.getKey() + "' detected, disabling previous instance");
                ((StateAware)existingDescriptor).disabled();
            }
            log.debug("Registering descriptor {}", descriptor.getClass().getName());
            registrations.add(targetBundleContext.registerService(ModuleDescriptor.class.getName(),
                    descriptor, null));

            if (reg.getI18nProperties() != null)
            {
                i18nPropertiesPluginManager.add(plugin.getKey(), reg.getI18nProperties());
            }
        }
        return new Registration()
        {
            @Override
            public void unregister()
            {
                for (ServiceRegistration reg : registrations)
                {
                    reg.unregister();
                }
            }
        };
    }
}
