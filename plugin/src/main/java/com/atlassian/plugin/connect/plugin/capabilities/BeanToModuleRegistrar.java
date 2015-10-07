package com.atlassian.plugin.connect.plugin.capabilities;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.connect.api.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleMeta;
import com.atlassian.plugin.connect.plugin.capabilities.provider.DefaultConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.descriptor.ConnectModuleProviderModuleDescriptor;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.connect.plugin.webhooks.PluginsWebHookProvider;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.predicate.ModuleDescriptorOfClassPredicate;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
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

import static com.atlassian.plugin.connect.modules.beans.WebHookModuleBean.newWebHookBean;

@Component
public class BeanToModuleRegistrar
{

    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;

    private final ConcurrentHashMap<String, DynamicDescriptorRegistration.Registration> registrations;
    private final ContainerManagedPlugin theConnectPlugin;
    private final PluginAccessor pluginAccessor;

    @Autowired
    public BeanToModuleRegistrar(DynamicDescriptorRegistration dynamicDescriptorRegistration,
                                 PluginRetrievalService pluginRetrievalService,
                                 PluginAccessor pluginAccessor)
    {
        this.dynamicDescriptorRegistration = dynamicDescriptorRegistration;
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

        List<DescriptorToRegister> descriptorsToRegister = new ArrayList<>();
        ConnectModuleProviderContext moduleProviderContext = new DefaultConnectModuleProviderContext(addon);
        getDescriptorsToRegisterForModules(addon.getModules(), theConnectPlugin, moduleProviderContext, descriptorsToRegister);

        List<ModuleBean> lifecycleWebhooks = getLifecycleWebhooks(addon.getLifecycle());
        Map<String, List<ModuleBean>> lifecycleWebhookModuleList
                = Collections.singletonMap(new WebHookModuleMeta().getDescriptorKey(), lifecycleWebhooks);
        getDescriptorsToRegisterForModules(lifecycleWebhookModuleList, theConnectPlugin, moduleProviderContext, descriptorsToRegister);

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

    private List<ModuleBean> getLifecycleWebhooks(LifecycleBean lifecycle)
    {
        List<ModuleBean> webhooks = new ArrayList<>();
        if (!Strings.isNullOrEmpty(lifecycle.getEnabled()))
        {
            webhooks.add(newWebHookBean().withEvent(PluginsWebHookProvider.CONNECT_ADDON_ENABLED).withUrl(lifecycle.getEnabled()).build());
        }
        if (!Strings.isNullOrEmpty(lifecycle.getDisabled()))
        {
            webhooks.add(newWebHookBean().withEvent(PluginsWebHookProvider.CONNECT_ADDON_DISABLED).withUrl(lifecycle.getDisabled()).build());
        }
        if (!Strings.isNullOrEmpty(lifecycle.getUninstalled()))
        {
            webhooks.add(newWebHookBean().withEvent(PluginsWebHookProvider.CONNECT_ADDON_UNINSTALLED).withUrl(lifecycle.getUninstalled()).build());
        }
        return webhooks;
    }

    private void getDescriptorsToRegisterForModules(Map<String, List<ModuleBean>> moduleList,
                                                    ContainerManagedPlugin theConnectPlugin,
                                                    ConnectModuleProviderContext moduleProviderContext,
                                                    List<DescriptorToRegister> descriptorsToRegister)
    {
        for (Map.Entry<String, List<ModuleBean>> entry : moduleList.entrySet())
        {
            List<ModuleBean> beans = entry.getValue();
            ConnectModuleProvider provider = findProvider(entry.getKey());
            List<ModuleDescriptor> descriptors = provider.createPluginModuleDescriptors(beans, theConnectPlugin, moduleProviderContext);
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
        Collection<ConnectModuleProvider> providers = pluginAccessor.getModules(
                new ModuleDescriptorOfClassPredicate<>(ConnectModuleProviderModuleDescriptor.class));
        Optional<ConnectModuleProvider> providerOptional = findProvider(descriptorKey, providers);
        if (!providerOptional.isPresent())
        {
            // Shouldn't happen, descriptor deserialization should have failed
            throw new IllegalStateException("Could not find module provider for descriptor registration");
        }
        return providerOptional.get();
    }

    private Optional<ConnectModuleProvider> findProvider(String descriptorKey, Collection<ConnectModuleProvider> providers)
    {
        // return Iterables.tryFind(providers, (provider) -> provider.getMeta().getDescriptorKey().equals(descriptorKey));
        for (ConnectModuleProvider provider : providers)
        {
            if (provider.getMeta().getDescriptorKey().equals(descriptorKey))
            {
                return Optional.of(provider);
            }

        }
        return Optional.absent();
    }
}
