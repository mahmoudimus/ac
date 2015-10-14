package com.atlassian.plugin.connect.plugin.integration.plugins;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.StateAware;
import com.atlassian.plugin.connect.api.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.connect.api.integration.plugins.DynamicDescriptorRegistration;
import com.google.common.collect.ImmutableList;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;

/**
 * Helper component that registers dynamic module descriptors
 */
@Component
public class DynamicDescriptorRegistrationImpl implements DynamicDescriptorRegistration
{
    private final BundleContext bundleContext;
    private static final Logger log = LoggerFactory.getLogger(DynamicDescriptorRegistrationImpl.class);

    @Autowired
    public DynamicDescriptorRegistrationImpl(BundleContext bundleContext)
    {
        this.bundleContext = bundleContext;
    }

    /**
     * Registers descriptors.  <strong>Important: make sure any osgi service references passed into
     * these descriptors are not proxies created by the p3 plugin, as it will cause ServiceProxyDestroyed
     * exceptions when the p3 plugin is upgraded.</strong>
     *
     * @param plugin the plugin for which to register descriptors
     * @param descriptors the module descriptors of the plugin
     * @return a representation of the descriptor registration
     */
    @Override
    public Registration registerDescriptors(Plugin plugin, DescriptorToRegister... descriptors)
    {
        return registerDescriptors(plugin, asList(descriptors));
    }

    /**
     * Registers descriptors.  
     *
     * @param plugin the plugin for which to register descriptors
     * @param descriptors the module descriptors of the plugin
     * @return a representation of the descriptor registration
     */
    @Override
    public Registration registerDescriptors(final Plugin plugin, Iterable<DescriptorToRegister> descriptors)
    {
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
            registrations.add(bundleContext.registerService(ModuleDescriptor.class.getName(),
                    descriptor, null));
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

            @Override
            public Collection<ModuleDescriptor<?>> getRegisteredDescriptors()
            {
                ImmutableList.Builder<ModuleDescriptor<?>> listBuilder = ImmutableList.builder();
                
                for (ServiceRegistration reg : registrations)
                {
                    ModuleDescriptor descriptor = (ModuleDescriptor) bundleContext.getService(reg.getReference());
                    
                    if(null != descriptor)
                    {
                        listBuilder.add(descriptor);
                    }
                }
                
                return listBuilder.build();
            }
        };
    }
}
