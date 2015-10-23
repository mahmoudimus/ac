package com.atlassian.plugin.connect.plugin.lifecycle;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.connect.api.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleMeta;
import com.atlassian.plugin.connect.plugin.request.webhook.PluginsWebHookProvider;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.predicate.ModuleDescriptorOfClassPredicate;
import com.google.common.base.Function;
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
import java.util.function.Predicate;
import java.util.function.Supplier;

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

    public synchronized void registerDescriptorsForBeans(ConnectAddonBean addon) throws ConnectModuleRegistrationException
    {
        //don't register modules more than once
        if (registrations.containsKey(addon.getKey()))
        {
            return;
        }

        Collection<ConnectModuleProvider> moduleProviders = pluginAccessor.getModules(
                new ModuleDescriptorOfClassPredicate<>(ConnectModuleProviderModuleDescriptor.class));
        ConnectModuleProviderContext moduleProviderContext = new DefaultConnectModuleProviderContext(addon);

        List<ModuleBean> lifecycleWebhooks = getLifecycleWebhooks(addon.getLifecycle());
        Map<String, List<ModuleBean>> lifecycleWebhookModuleList
                = Collections.singletonMap(new WebHookModuleMeta().getDescriptorKey(), lifecycleWebhooks);

        List<DescriptorToRegister> descriptorsToRegister = new ArrayList<>();
        getDescriptorsToRegisterForModules(addon.getModules(), moduleProviderContext, moduleProviders, descriptorsToRegister);
        getDescriptorsToRegisterForModules(lifecycleWebhookModuleList, moduleProviderContext, moduleProviders, descriptorsToRegister);
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
            ConnectModuleProviderContext moduleProviderContext,
            Collection<ConnectModuleProvider> moduleProviders,
            List<DescriptorToRegister> descriptorsToRegister) throws ConnectModuleRegistrationException
    {
        for (Map.Entry<String, List<ModuleBean>> entry : moduleList.entrySet())
        {
            List<ModuleBean> beans = entry.getValue();
            ConnectModuleProvider provider = findProviderOrThrow(entry.getKey(), moduleProviders);
            List<ModuleDescriptor> descriptors = provider.createPluginModuleDescriptors(beans, moduleProviderContext);
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

    private ConnectModuleProvider findProviderOrThrow(String descriptorKey, Collection<ConnectModuleProvider> moduleProviders)
            throws ConnectModuleRegistrationException
    {
        return moduleProviders.stream()
                .filter(new Predicate<ConnectModuleProvider>()
                {
                    @Override
                    public boolean test(ConnectModuleProvider provider)
                    {
                        return provider.getMeta().getDescriptorKey().equals(descriptorKey);
                    }
                })
                .findFirst()
                .orElseThrow(new Supplier<ConnectModuleRegistrationException>()
                {
                    @Override
                    public ConnectModuleRegistrationException get()
                    {
                        return new ConnectModuleRegistrationException("Could not find module provider for descriptor registration");
                    }
                });
    }
}
