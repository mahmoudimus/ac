package com.atlassian.plugin.connect.plugin.capabilities;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.util.ProductFilter;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.connect.plugin.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.connect.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.provider.BeanTransformContext;
import com.atlassian.plugin.connect.plugin.module.provider.ModuleListProviderContainer;
import com.atlassian.plugin.connect.plugin.module.provider.ModuleListProviderFactory;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BeanToModuleRegistrar
{
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final ConcurrentHashMap<String, DynamicDescriptorRegistration.Registration> registrations;
    private final ContainerManagedPlugin theConnectPlugin;
    private final ApplicationProperties applicationProperties;
    private final ModuleListProviderContainer moduleListProviderContainer;

    @Autowired
    public BeanToModuleRegistrar(DynamicDescriptorRegistration dynamicDescriptorRegistration,
            PluginRetrievalService pluginRetrievalService, ApplicationProperties applicationProperties,
            ModuleListProviderContainer moduleListProviderContainer)
    {
        this.dynamicDescriptorRegistration = dynamicDescriptorRegistration;
        this.applicationProperties = applicationProperties;
        this.moduleListProviderContainer = moduleListProviderContainer;
        this.theConnectPlugin = (ContainerManagedPlugin) pluginRetrievalService.getPlugin();
        this.registrations = new ConcurrentHashMap<String, DynamicDescriptorRegistration.Registration>();
    }

    public synchronized void registerDescriptorsForBeans(ConnectAddonBean addon) throws InvalidDescriptorException
    {
        //don't register modules more than once
        if (registrations.containsKey(addon.getKey()))
        {
            return;
        }

        List<DescriptorToRegister> descriptorsToRegister = new ArrayList<DescriptorToRegister>();

        BeanTransformContext ctx = new BeanTransformContext(theConnectPlugin, ProductFilter.valueOf(applicationProperties.getDisplayName().toUpperCase()));

        //now process the module fields
        processFields(addon, ctx, descriptorsToRegister);


        if (!descriptorsToRegister.isEmpty())
        {
            registrations.putIfAbsent(addon.getKey(), dynamicDescriptorRegistration.registerDescriptors(theConnectPlugin, descriptorsToRegister));
        }
    }

    public synchronized void unregisterDescriptorsForAddon(String addonKey)
    {
        if (registrations.containsKey(addonKey))
        {
            DynamicDescriptorRegistration.Registration reg = registrations.remove(addonKey);

            if (null != reg)
            {
                try
                {
                    reg.unregister();
                }
                catch (IllegalStateException e)
                {
                    //service was already unregistered, just ignore
                }
            }
        }
    }
    
    public Collection<ModuleDescriptor<?>> getRegisteredDescriptorsForAddon(String addonKey)
    {
        if (registrations.containsKey(addonKey))
        {
            DynamicDescriptorRegistration.Registration reg = registrations.get(addonKey);
            return reg.getRegisteredDescriptors();
        }
        
        return Collections.emptyList();
    }
    
    public boolean descriptorsAreRegistered(String pluginKey)
    {
        return registrations.containsKey(pluginKey);
    }

    private void processFields(ConnectAddonBean addon, BeanTransformContext ctx, List<DescriptorToRegister> descriptorsToRegister)
    {
        final Iterable<ModuleListProviderFactory> moduleListProviderFactories =
                moduleListProviderContainer.provideFactories(addon);

        for (ModuleListProviderFactory moduleListProviderFactory : moduleListProviderFactories)
        {
            List<DescriptorToRegister> toRegister = moduleListProviderFactory.getDescriptors(addon, ctx);
            descriptorsToRegister.addAll(toRegister);
        }
    }
}
