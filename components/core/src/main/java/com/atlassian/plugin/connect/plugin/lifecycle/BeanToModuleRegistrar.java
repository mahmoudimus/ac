package com.atlassian.plugin.connect.plugin.lifecycle;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.lifecycle.DynamicDescriptorRegistration;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleMeta;
import com.atlassian.plugin.connect.plugin.descriptor.event.EventPublishingModuleValidationExceptionHandler;
import com.atlassian.plugin.connect.plugin.lifecycle.event.PluginsWebHookProvider;
import com.atlassian.plugin.connect.spi.lifecycle.ConnectModuleProvider;
import com.atlassian.plugin.predicate.ModuleDescriptorOfClassPredicate;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.atlassian.plugin.connect.modules.beans.WebHookModuleBean.newWebHookBean;

@Component
public class BeanToModuleRegistrar
{

    private static final Logger log = LoggerFactory.getLogger(BeanToModuleRegistrar.class);

    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final PluginAccessor pluginAccessor;
    private final EventPublisher eventPublisher;

    private final ConcurrentMap<String, DynamicDescriptorRegistration.Registration> registrations = new ConcurrentHashMap<>();

    @Autowired
    public BeanToModuleRegistrar(DynamicDescriptorRegistration dynamicDescriptorRegistration,
            PluginAccessor pluginAccessor,
            EventPublisher eventPublisher)
    {
        this.dynamicDescriptorRegistration = dynamicDescriptorRegistration;
        this.pluginAccessor = pluginAccessor;
        this.eventPublisher = eventPublisher;
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

        Map<String, List<ModuleBean>> moduleLists = getModuleLists(addon);
        List<ModuleBean> lifecycleWebhooks = getLifecycleWebhooks(addon.getLifecycle());
        Map<String, List<ModuleBean>> lifecycleWebhookModuleList
                = Collections.singletonMap(new WebHookModuleMeta().getDescriptorKey(), lifecycleWebhooks);

        List<ModuleDescriptor<?>> descriptorsToRegister = new ArrayList<>();
        getDescriptorsToRegisterForModules(moduleLists, addon, moduleProviders, descriptorsToRegister);
        getDescriptorsToRegisterForModules(lifecycleWebhookModuleList, addon, moduleProviders, descriptorsToRegister);
        if (!descriptorsToRegister.isEmpty())
        {
            registrations.putIfAbsent(addon.getKey(), dynamicDescriptorRegistration.registerDescriptors(descriptorsToRegister));
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
                    log.warn(String.format("Attempted to unregister dynamic descriptors for add-on %s but failed", addonKey), e);
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
            ConnectAddonBean addon,
            Collection<ConnectModuleProvider> moduleProviders,
            List<ModuleDescriptor<?>> descriptorsToRegister) throws ConnectModuleRegistrationException
    {
        for (Map.Entry<String, List<ModuleBean>> entry : moduleList.entrySet())
        {
            List<ModuleBean> beans = entry.getValue();
            ConnectModuleProvider provider = findProviderOrThrow(entry.getKey(), moduleProviders);
            List<ModuleDescriptor<?>> descriptors = provider.createPluginModuleDescriptors(beans, addon);
            descriptorsToRegister.addAll(descriptors);
        }
    }

    private Map<String, List<ModuleBean>> getModuleLists(ConnectAddonBean addon) throws ConnectModuleRegistrationException
    {
        return addon.getModules().getValidModuleLists(new EventPublishingModuleValidationExceptionHandler(eventPublisher)
        {

            @Override
            protected void handleModuleValidationCause(ConnectModuleValidationException cause)
            {
                super.handleModuleValidationCause(cause);

                String message = String.format("Descriptor validation failed while enabling add-on %s, skipping", addon.getKey());
                throw new ConnectModuleRegistrationException(message, cause);
            }
        });
    }

    private ConnectModuleProvider findProviderOrThrow(String descriptorKey, Collection<ConnectModuleProvider> moduleProviders)
            throws ConnectModuleRegistrationException
    {
        return moduleProviders.stream()
                .filter(provider -> provider.getMeta().getDescriptorKey().equals(descriptorKey))
                .findFirst()
                .orElseThrow(() -> new ConnectModuleRegistrationException(String.format("Could not find module provider %s for descriptor registration", descriptorKey)));
    }
}
