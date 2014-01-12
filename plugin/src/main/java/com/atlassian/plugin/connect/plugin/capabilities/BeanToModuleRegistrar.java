package com.atlassian.plugin.connect.plugin.capabilities;

import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.annotation.ConnectModule;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.LifecycleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ModuleList;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.plugin.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.connect.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.AutowireWithConnectPluginDecorator;
import com.atlassian.plugin.connect.plugin.webhooks.PluginsWebHookProvider;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.module.ContainerAccessor;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.osgi.factory.OsgiPlugin;
import com.atlassian.plugin.spring.scanner.ProductFilter;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebHookModuleBean.newWebHookBean;
import static com.atlassian.plugin.connect.plugin.capabilities.util.ConnectReflectionHelper.isParameterizedListWithType;
import static com.google.common.collect.Lists.newArrayList;

@Component
public class BeanToModuleRegistrar
{
    private static final String WEBHOOKS_FIELD = "webhooks";
    
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;

    private final ConcurrentHashMap<String, DynamicDescriptorRegistration.Registration> registrations;
    private final ProductAccessor productAccessor;
    private final ContainerManagedPlugin theConnectPlugin;
    private final ApplicationProperties applicationProperties;

    @Autowired
    public BeanToModuleRegistrar(DynamicDescriptorRegistration dynamicDescriptorRegistration, PluginRetrievalService pluginRetrievalService, ProductAccessor productAccessor, ApplicationProperties applicationProperties)
    {
        this.dynamicDescriptorRegistration = dynamicDescriptorRegistration;
        this.productAccessor = productAccessor;
        this.applicationProperties = applicationProperties;
        this.theConnectPlugin = (ContainerManagedPlugin) pluginRetrievalService.getPlugin();
        this.registrations = new ConcurrentHashMap<String, DynamicDescriptorRegistration.Registration>();
    }

    public void registerDescriptorsForBeans(Plugin plugin, ConnectAddonBean addon)
    {
        BundleContext addonBundleContext = ((OsgiPlugin) plugin).getBundle().getBundleContext();
        AutowireWithConnectPluginDecorator connectAutowiringPlugin = new AutowireWithConnectPluginDecorator((AutowireCapablePlugin) theConnectPlugin, plugin, Sets.<Class<?>>newHashSet(productAccessor.getConditions().values()));
        List<DescriptorToRegister> descriptorsToRegister = new ArrayList<DescriptorToRegister>();

        BeanTransformContext ctx = new BeanTransformContext(connectAutowiringPlugin, addonBundleContext, ProductFilter.valueOf(applicationProperties.getDisplayName().toUpperCase()));

        //we MUST add in the lifecycle webhooks first
        ModuleList moduleList = getCapabilitiesWithLifecycleWebhooks(addon);
        
        //now process the module fields
        processFields(moduleList, ctx, descriptorsToRegister);


        if (!descriptorsToRegister.isEmpty())
        {
            registrations.putIfAbsent(plugin.getKey(), dynamicDescriptorRegistration.registerDescriptors(plugin, descriptorsToRegister));
        }
    }

    private ModuleList getCapabilitiesWithLifecycleWebhooks(ConnectAddonBean addon)
    {
        LifecycleBean lifecycle = addon.getLifecycle();
        ConnectAddonBeanBuilder builder = newConnectAddonBean(addon);
        
        if(!Strings.isNullOrEmpty(lifecycle.getEnabled()))
        {
            //add webhook
            builder.withModule(WEBHOOKS_FIELD, newWebHookBean().withEvent(PluginsWebHookProvider.CONNECT_ADDON_ENABLED).withUrl(lifecycle.getEnabled()).build());
        }
        if(!Strings.isNullOrEmpty(lifecycle.getDisabled()))
        {
            //add webhook
            builder.withModule(WEBHOOKS_FIELD, newWebHookBean().withEvent(PluginsWebHookProvider.CONNECT_ADDON_DISABLED).withUrl(lifecycle.getDisabled()).build());
        }
        if(!Strings.isNullOrEmpty(lifecycle.getUninstalled()))
        {
            //add webhook
            builder.withModule(WEBHOOKS_FIELD, newWebHookBean().withEvent(PluginsWebHookProvider.CONNECT_ADDON_UNINSTALLED).withUrl(lifecycle.getUninstalled()).build());
        }
        
        return builder.build().getModules();
    }

    private void processFields(ModuleList moduleList, BeanTransformContext ctx, List<DescriptorToRegister> descriptorsToRegister)
    {
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
                        beanList = moduleBean == null ? ImmutableList.<ModuleBean>of() : newArrayList(moduleBean);
                    }

                    descriptorsToRegister.addAll(getDescriptors(ctx, field.getName(), anno, beanList));
                }
                catch (IllegalAccessException e)
                {
                    //ignore. this should never happen
                }
            }
        }
    }


    private List<DescriptorToRegister> getDescriptors(BeanTransformContext ctx, String jsonFieldName, ConnectModule providerAnnotation, List<? extends ModuleBean> beans)
    {
        List<ProductFilter> products = Arrays.asList(providerAnnotation.products());
        if (products.contains(ProductFilter.ALL) || (null != ctx.getAppFilter() && products.contains(ctx.getAppFilter())))
        {
            ContainerAccessor accessor = theConnectPlugin.getContainerAccessor();
            Collection<? extends ConnectModuleProvider> providers = accessor.getBeansOfType(providerAnnotation.value());
            if (!providers.isEmpty())
            {
                ConnectModuleProvider provider = providers.iterator().next();
                return Lists.transform(provider.provideModules(ctx.getConnectAutowiringPlugin(), ctx.getAddonBundleContext(), jsonFieldName, beans), new Function<ModuleDescriptor, DescriptorToRegister>()
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

    public void unregisterDescriptorsForPlugin(Plugin plugin)
    {
        if (registrations.containsKey(plugin.getKey()))
        {
            DynamicDescriptorRegistration.Registration reg = registrations.get(plugin.getKey());
            try
            {
                reg.unregister();
            }
            catch (IllegalStateException e)
            {
                //service was already unregistered, just ignore
            }

            registrations.remove(plugin.getKey());
        }
    }

    private class BeanTransformContext
    {
        private final AutowireWithConnectPluginDecorator connectAutowiringPlugin;
        private final BundleContext addonBundleContext;
        private final ProductFilter appFilter;

        private BeanTransformContext(AutowireWithConnectPluginDecorator connectAutowiringPlugin, BundleContext addonBundleContext, ProductFilter appFilter)
        {
            this.connectAutowiringPlugin = connectAutowiringPlugin;
            this.addonBundleContext = addonBundleContext;
            this.appFilter = appFilter;
        }

        private AutowireWithConnectPluginDecorator getConnectAutowiringPlugin()
        {
            return connectAutowiringPlugin;
        }

        private BundleContext getAddonBundleContext()
        {
            return addonBundleContext;
        }

        private ProductFilter getAppFilter()
        {
            return appFilter;
        }
    }
}
