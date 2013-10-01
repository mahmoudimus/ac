package com.atlassian.plugin.connect.plugin.capabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.api.capabilities.beans.CapabilityBean;
import com.atlassian.plugin.connect.api.capabilities.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.plugin.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.connect.plugin.integration.plugins.DynamicDescriptorRegistration;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BeanToModuleRegistrar
{
    private final PluginAccessor pluginAccessor;
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    
    private final ConcurrentHashMap<String,DynamicDescriptorRegistration.Registration> registrations;
    
    @Autowired
    public BeanToModuleRegistrar(PluginAccessor pluginAccessor, DynamicDescriptorRegistration dynamicDescriptorRegistration)
    {
        this.pluginAccessor = pluginAccessor;
        this.dynamicDescriptorRegistration = dynamicDescriptorRegistration;
        this.registrations = new ConcurrentHashMap<String, DynamicDescriptorRegistration.Registration>();
    }
    
    public void registerDescriptorsForBeans(Plugin plugin, List<CapabilityBean> beans)
    {
        List<DescriptorToRegister> descriptorsToRegister = new ArrayList<DescriptorToRegister>();
        for(CapabilityBean bean :beans)
        {
            if(bean.getClass().isAnnotationPresent(CapabilitySet.class))
            {
                Class<? extends ConnectModuleProvider> providerClass = bean.getClass().getAnnotation(CapabilitySet.class).moduleProvider();
                List<? extends ConnectModuleProvider> providers = pluginAccessor.getEnabledModulesByClass(providerClass);
                
                if(!providers.isEmpty())
                {
                    ConnectModuleProvider provider = providers.get(0);
                    descriptorsToRegister.addAll(Lists.transform(provider.provideModules(plugin,beans),new Function<ModuleDescriptor,DescriptorToRegister>() {
                        @Override
                        public DescriptorToRegister apply(@Nullable ModuleDescriptor input)
                        {
                            return new DescriptorToRegister(input);
                        }
                    }));
                }
            }
        }
        
        if(!descriptorsToRegister.isEmpty())
        {
            registrations.putIfAbsent(plugin.getKey(),dynamicDescriptorRegistration.registerDescriptors(plugin,descriptorsToRegister));
        }
        
    }
    
    public void unregisterDescriptorsForPlugin(Plugin plugin)
    {
        if(registrations.containsKey(plugin.getKey()))
        {
            DynamicDescriptorRegistration.Registration reg = registrations.get(plugin.getKey());
            reg.unregister();
            
            registrations.remove(plugin.getKey());
        }
    }
}
