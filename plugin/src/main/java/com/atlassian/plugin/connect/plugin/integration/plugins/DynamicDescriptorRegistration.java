package com.atlassian.plugin.connect.plugin.integration.plugins;

import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.StateAware;
import com.atlassian.plugin.connect.plugin.util.BundleUtil;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;

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
