package com.atlassian.plugin.connect.plugin.capabilities;

import com.atlassian.jira.plugin.util.ModuleDescriptors;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.connect.modules.annotation.ConnectModule;
import com.atlassian.plugin.connect.modules.beans.*;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.modules.util.ProductFilter;
import com.atlassian.plugin.connect.plugin.capabilities.provider.DefaultConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.connect.plugin.exception.ModuleProviderNotFoundException;
import com.atlassian.plugin.connect.api.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.connect.plugin.webhooks.PluginsWebHookProvider;
import com.atlassian.plugin.connect.spi.iframe.context.module.ConnectContextParameterResolverModuleDescriptor;
import com.atlassian.plugin.connect.spi.module.ContextParametersValidator;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderModuleDescriptor;
import com.atlassian.plugin.connect.spi.module.provider.ModuleListProviderContainer;
import com.atlassian.plugin.module.ContainerAccessor;
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
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WebHookModuleBean.newWebHookBean;
import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.isParameterizedListWithType;

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
        ModuleList moduleList = getCapabilitiesWithLifecycleWebhooks(addon);

        //now process the module fields
        processFields(addon, moduleList, ctx, descriptorsToRegister);


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

    private ModuleList getCapabilitiesWithLifecycleWebhooks(ConnectAddonBean addon)
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

    private void processFields(ConnectAddonBean addon, ModuleList moduleList, BeanTransformContext ctx, List<DescriptorToRegister> descriptorsToRegister)
    {
        //Collection<ModuleDescriptor<?>> descriptors = theConnectPlugin.getModuleDescriptors();
        Collection<ConnectContextParameterResolverModuleDescriptor.ConnectContextParametersResolver> collection1 = pluginAccessor.getModules(new ModuleDescriptorOfClassPredicate<>(ConnectContextParameterResolverModuleDescriptor.class));
        Collection<ConnectModuleProvider> collection = pluginAccessor.getModules(new ModuleDescriptorOfClassPredicate<ConnectModuleProvider>(ConnectModuleProviderModuleDescriptor.class));
//        for (Map.Entry<Class<? extends BaseModuleBean>, List<Module>> entry : moduleList.entries())
//        {
//            ConnectModuleProvider moduleProvider = connectModuleProviderRegistry.get(entry.key());
//            List<ModuleDescriptor> descriptors = provider.provideModules(new DefaultConnectModuleProviderContext(addon), entry.value());
//            descriptorsToRegister.addAll(transform(descriptors));
//        }
        int i = 0;

        
        for (Map.Entry<String,List<JsonObject>> entry : addon.getTestModules().entrySet())
        {
            boolean providerFound = false;
            for (ConnectModuleProvider provider : collection)
            {
                if(provider.getClass().getSimpleName().contains(entry.getKey().substring(0, entry.getKey().length() - 1)))
                {
                    providerFound = true;
                }
            }
            if(!providerFound)
            {
                // BAD NEWS, FAIL!
            }
        }

        for (Map.Entry<String,List<JsonObject>> entry : addon.getTestModules().entrySet())
        {
            for (ConnectModuleProvider provider : collection)
            {
                if(provider.getClass().getSimpleName().toLowerCase().contains(entry.getKey().toLowerCase().substring(0, entry.getKey().length() - 1)))
                {
                    //String test = entry.getValue().get(0).getAsString();
                    JsonObject testJson = entry.getValue().get(0);
                    Set testSet = testJson.entrySet();
                    int k = 0;
//                    List<ModuleDescriptor> descriptors = provider.provideModules(new DefaultConnectModuleProviderContext(addon),
//                            ctx.getTheConnectPlugin(), entry.getKey(), entry.getValue());
//                    List<DescriptorToRegister> theseDescriptors = Lists.transform(descriptors, new Function<ModuleDescriptor, DescriptorToRegister>()
//                    {
//                        @Override
//                        public DescriptorToRegister apply(@Nullable ModuleDescriptor input)
//                        {
//                            return new DescriptorToRegister(input);
//                        }
//                    });
//                    descriptorsToRegister.addAll(theseDescriptors);
                }
            }
        }
        
//        for (ConnectModuleProvider provider : collection)
//        {
//            provider.provideModules().
//        }
//
//        for()
        
        for (Field field : moduleList.getClass().getDeclaredFields())
        {
            if (field.isAnnotationPresent(ConnectModule.class))
            {
                try
                {
                    ConnectModule anno = field.getAnnotation(ConnectModule.class);
                    field.setAccessible(true);

                    Type fieldType = field.getGenericType();

                    List<? extends ModuleBean> beanList;

                    if (isParameterizedListWithType(fieldType, ModuleBean.class))
                    {
                        beanList = (List<? extends ModuleBean>) field.get(moduleList);
                    }
                    else
                    {
                        ModuleBean moduleBean = (ModuleBean) field.get(moduleList);
                        beanList = moduleBean == null ? Collections.EMPTY_LIST : Collections.singletonList(moduleBean);
                    }

                    List<DescriptorToRegister> registerMe = getDescriptors(addon, ctx, field.getName(), anno, beanList);
                    descriptorsToRegister.addAll(registerMe);
                }
                catch (IllegalAccessException e)
                {
                    //ignore. this should never happen
                }
            }
        }
    }


    private List<DescriptorToRegister> getDescriptors(ConnectAddonBean addon, BeanTransformContext ctx, String jsonFieldName, ConnectModule providerAnnotation, List<? extends ModuleBean> beans)
    {
        List<ProductFilter> products = Arrays.asList(providerAnnotation.products());
        if (products.contains(ProductFilter.ALL) || (null != ctx.getAppFilter() && products.contains(ctx.getAppFilter())))
        {
            Class<? extends ConnectModuleProvider> theProviderClass = null;
            try
            {
                theProviderClass = (Class<? extends ConnectModuleProvider>) Class.forName(providerAnnotation.value());
            }
            catch (ClassNotFoundException e)
            {
                throw new ModuleProviderNotFoundException("Unable to load module provider for class [" + providerAnnotation.value() + "]", e);
            }

            ContainerAccessor accessor = theConnectPlugin.getContainerAccessor();
            Collection<? extends ConnectModuleProvider> providers = accessor.getBeansOfType(theProviderClass);


            if (!providers.isEmpty())
            {
                ConnectModuleProvider provider = providers.iterator().next();

                List<ModuleDescriptor> descriptors = provider.provideModules(new DefaultConnectModuleProviderContext(addon),
                        ctx.getTheConnectPlugin(), jsonFieldName, beans);
                
                return Lists.transform(descriptors, new Function<ModuleDescriptor, DescriptorToRegister>()
                {
                    @Override
                    public DescriptorToRegister apply(@Nullable ModuleDescriptor input)
                    {
                        return new DescriptorToRegister(input);
                    }
                });
            }
            else
            {
                return Collections.EMPTY_LIST;
            }
        }
        else
        {
            return Collections.EMPTY_LIST;
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
