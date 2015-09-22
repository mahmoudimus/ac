package com.atlassian.plugin.connect.plugin.capabilities;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.connect.api.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleMeta;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.modules.util.ProductFilter;
import com.atlassian.plugin.connect.plugin.capabilities.provider.DefaultConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.connect.plugin.webhooks.PluginsWebHookProvider;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderModuleDescriptor;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.predicate.ModuleDescriptorOfClassPredicate;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WebHookModuleBean.newWebHookBean;

@Component
public class BeanToModuleRegistrar
{

    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;

    private final ConcurrentHashMap<String, DynamicDescriptorRegistration.Registration> registrations;
    private final ContainerManagedPlugin theConnectPlugin;
    private final ApplicationProperties applicationProperties;
    private final PluginAccessor pluginAccessor;

    @Autowired
    public BeanToModuleRegistrar(DynamicDescriptorRegistration dynamicDescriptorRegistration,
                                 PluginRetrievalService pluginRetrievalService,
                                 ApplicationProperties applicationProperties,
                                 PluginAccessor pluginAccessor)
    {
        this.dynamicDescriptorRegistration = dynamicDescriptorRegistration;
        this.applicationProperties = applicationProperties;
        this.theConnectPlugin = (ContainerManagedPlugin) pluginRetrievalService.getPlugin();
        this.registrations = new ConcurrentHashMap<>();
        this.pluginAccessor = pluginAccessor;
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

        //we MUST add in the lifecycle webhooks first
        addon = getCapabilitiesWithLifecycleWebhooks(addon);

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
        
        return Collections.EMPTY_LIST;
    }
    
    public boolean descriptorsAreRegistered(String pluginKey)
    {
        return registrations.containsKey(pluginKey);
    }

    private ConnectAddonBean getCapabilitiesWithLifecycleWebhooks(ConnectAddonBean addon)
    {
        LifecycleBean lifecycle = addon.getLifecycle();
        ConnectAddonBeanBuilder builder = newConnectAddonBean(addon);

        WebHookModuleMeta meta = new WebHookModuleMeta();
        if (!Strings.isNullOrEmpty(lifecycle.getEnabled()))
        {
            //add webhook
            builder.withModule(meta.getDescriptorKey(), newWebHookBean().withEvent(PluginsWebHookProvider.CONNECT_ADDON_ENABLED).withUrl(lifecycle.getEnabled()).build());
        }
        if (!Strings.isNullOrEmpty(lifecycle.getDisabled()))
        {
            //add webhook
            builder.withModule(meta.getDescriptorKey(), newWebHookBean().withEvent(PluginsWebHookProvider.CONNECT_ADDON_DISABLED).withUrl(lifecycle.getDisabled()).build());
        }
        if (!Strings.isNullOrEmpty(lifecycle.getUninstalled()))
        {
            //add webhook
            builder.withModule(meta.getDescriptorKey(), newWebHookBean().withEvent(PluginsWebHookProvider.CONNECT_ADDON_UNINSTALLED).withUrl(lifecycle.getUninstalled()).build());
        }

        return builder.build();
    }
    


    private void processFields(ConnectAddonBean addon, BeanTransformContext ctx, List<DescriptorToRegister> descriptorsToRegister)
    {
        for (Map.Entry<String,Supplier<List<ModuleBean>>> entry : addon.getModules().entrySet())
        {
            List<ModuleBean> beans = entry.getValue().get();
            ConnectModuleProvider provider = findProvider(entry.getKey());
            List<ModuleDescriptor> descriptors = provider.provideModules(new DefaultConnectModuleProviderContext(addon), ctx.getTheConnectPlugin(), beans);
            List<DescriptorToRegister> theseDescriptors = Lists.transform(descriptors, new Function<ModuleDescriptor, DescriptorToRegister>()
            {
                @Override
                public DescriptorToRegister apply(@Nullable ModuleDescriptor input)
                {
                    return new DescriptorToRegister(input);
                }
            });
            descriptorsToRegister.addAll(theseDescriptors);
        }
    }

    private ConnectModuleProvider findProvider(String descriptorKey)
    {
        Collection<ConnectModuleProvider> providers = pluginAccessor.getModules(new ModuleDescriptorOfClassPredicate<>(ConnectModuleProviderModuleDescriptor.class));

        for(ConnectModuleProvider provider: providers)
        {
            if(provider.getMeta().getDescriptorKey().equals(descriptorKey))
            {
                return provider;
            }
        }
        return null;
    }

    private class BeanTransformContext
    {
        private final Plugin theConnectPlugin;
        private final ProductFilter appFilter;

        private BeanTransformContext(Plugin theConnectPlugin, ProductFilter appFilter)
        {
            this.theConnectPlugin = theConnectPlugin;
            this.appFilter = appFilter;
        }

        private Plugin getTheConnectPlugin()
        {
            return theConnectPlugin;
        }

        private ProductFilter getAppFilter()
        {
            return appFilter;
        }
    }
}
