package com.atlassian.plugin.connect.plugin.capabilities;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.connect.modules.beans.*;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.modules.util.ProductFilter;
import com.atlassian.plugin.connect.plugin.capabilities.provider.DefaultConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.connect.api.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.connect.plugin.webhooks.PluginsWebHookProvider;
import com.atlassian.plugin.connect.spi.iframe.context.module.ConnectContextParameterResolverModuleDescriptor;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderModuleDescriptor;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.predicate.ModuleDescriptorOfClassPredicate;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WebHookModuleBean.newWebHookBean;

@Component
public class BeanToModuleRegistrar
{
    private static final String WEBHOOKS_FIELD = "webhooks";

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
        Map<String, List<JsonObject>> moduleList = getCapabilitiesWithLifecycleWebhooks(addon);

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

    private Map<String, List<JsonObject>> getCapabilitiesWithLifecycleWebhooks(ConnectAddonBean addon)
    {
        LifecycleBean lifecycle = addon.getLifecycle();
        ConnectAddonBeanBuilder builder = newConnectAddonBean(addon);

        if (!Strings.isNullOrEmpty(lifecycle.getEnabled()))
        {
            //add webhook
            builder.withModule(WEBHOOKS_FIELD, newWebHookBean().withEvent(PluginsWebHookProvider.CONNECT_ADDON_ENABLED).withUrl(lifecycle.getEnabled()).build());
        }
        if (!Strings.isNullOrEmpty(lifecycle.getDisabled()))
        {
            //add webhook
            builder.withModule(WEBHOOKS_FIELD, newWebHookBean().withEvent(PluginsWebHookProvider.CONNECT_ADDON_DISABLED).withUrl(lifecycle.getDisabled()).build());
        }
        if (!Strings.isNullOrEmpty(lifecycle.getUninstalled()))
        {
            //add webhook
            builder.withModule(WEBHOOKS_FIELD, newWebHookBean().withEvent(PluginsWebHookProvider.CONNECT_ADDON_UNINSTALLED).withUrl(lifecycle.getUninstalled()).build());
        }

        return builder.build().getModules();
    }
    
    private ConnectModuleProvider findProvider(String descriptorKey, Collection<ConnectModuleProvider> providers)
    {
        for(ConnectModuleProvider provider: providers)
        {
            if(provider.getDescriptorKey().equals(descriptorKey))
            {
                return provider;
            }
        }
        return null;        
    }

    private void processFields(ConnectAddonBean addon, BeanTransformContext ctx, List<DescriptorToRegister> descriptorsToRegister)
    {
        Collection<ConnectContextParameterResolverModuleDescriptor.ConnectContextParametersResolver> collection1 = pluginAccessor.getModules(new ModuleDescriptorOfClassPredicate<>(ConnectContextParameterResolverModuleDescriptor.class));
        Collection<ConnectModuleProvider> providers = pluginAccessor.getModules(new ModuleDescriptorOfClassPredicate<>(ConnectModuleProviderModuleDescriptor.class));

        Map<String, List<? extends BaseModuleBean>> beanMap = new HashMap<>();
        
        for (Map.Entry<String,List<JsonObject>> entry : addon.getModules().entrySet())
        {
            ConnectModuleProvider provider = findProvider(entry.getKey(), providers);
            if(provider == null)
            {
                // BAD NEWS, FAIL!
            }

            List<? extends BaseModuleBean> beans = provider.validate(entry.getValue());
            if(beans == null)
            {
                // BAD NEWS, FAIL!
            }
            beanMap.put(provider.getDescriptorKey(), beans);
        }
        
        addon.setModuleBeans(beanMap);

        for (Map.Entry<String,List<? extends BaseModuleBean>> entry : addon.getModuleBeans().entrySet())
        {
            List<? extends BaseModuleBean> beans = entry.getValue();
            ConnectModuleProvider provider = findProvider(entry.getKey(), providers);
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
