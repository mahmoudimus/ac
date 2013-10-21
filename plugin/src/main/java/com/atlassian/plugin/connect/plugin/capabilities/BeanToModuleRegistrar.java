package com.atlassian.plugin.connect.plugin.capabilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.plugin.capabilities.beans.CapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.plugin.capabilities.provider.NullModuleProvider;
import com.atlassian.plugin.connect.plugin.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.connect.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.ConditionLoadingPlugin;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.module.ContainerAccessor;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.osgi.factory.OsgiPlugin;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.collect.Lists.newArrayList;

@Component
public class BeanToModuleRegistrar
{
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;

    private final ConcurrentHashMap<String, DynamicDescriptorRegistration.Registration> registrations;
    private final ProductAccessor productAccessor;
    private final ContainerManagedPlugin theConnectPlugin;

    @Autowired
    public BeanToModuleRegistrar(DynamicDescriptorRegistration dynamicDescriptorRegistration, PluginRetrievalService pluginRetrievalService, ProductAccessor productAccessor)
    {
        this.dynamicDescriptorRegistration = dynamicDescriptorRegistration;
        this.productAccessor = productAccessor;
        this.theConnectPlugin = (ContainerManagedPlugin) pluginRetrievalService.getPlugin();
        this.registrations = new ConcurrentHashMap<String, DynamicDescriptorRegistration.Registration>();
    }

    //TODO: change this to use the capability map instead of the raw list
    public void registerDescriptorsForBeans(Plugin plugin, List<CapabilityBean> beans)
    {
        BundleContext addonBundleContext = ((OsgiPlugin) plugin).getBundle().getBundleContext();
        
        List<DescriptorToRegister> descriptorsToRegister = new ArrayList<DescriptorToRegister>();
        ContainerAccessor accessor = theConnectPlugin.getContainerAccessor();
        for (CapabilityBean bean : beans)
        {
            if (bean.getClass().isAnnotationPresent(CapabilitySet.class))
            {
                Class<? extends ConnectModuleProvider> providerClass = bean.getClass().getAnnotation(CapabilitySet.class).moduleProvider();
                
                if(NullModuleProvider.class.isAssignableFrom(providerClass))
                {
                    continue;
                }

                Collection<? extends ConnectModuleProvider> providers = accessor.getBeansOfType(providerClass);

                if (!providers.isEmpty())
                {
                    ConnectModuleProvider provider = providers.iterator().next();
                    descriptorsToRegister.addAll(Lists.transform(provider.provideModules(plugin,addonBundleContext, newArrayList(bean)), new Function<ModuleDescriptor, DescriptorToRegister>()
                    {
                        @Override
                        public DescriptorToRegister apply(@Nullable ModuleDescriptor input)
                        {
                            return new DescriptorToRegister(input);
                        }
                    }));
                }
            }
        }

        if (!descriptorsToRegister.isEmpty())
        {
            registrations.putIfAbsent(plugin.getKey(), dynamicDescriptorRegistration.registerDescriptors(plugin, descriptorsToRegister));
        }

    }

    public void unregisterDescriptorsForPlugin(Plugin plugin)
    {
        if (registrations.containsKey(plugin.getKey()))
        {
            DynamicDescriptorRegistration.Registration reg = registrations.get(plugin.getKey());
            reg.unregister();

            registrations.remove(plugin.getKey());
        }
    }
}
