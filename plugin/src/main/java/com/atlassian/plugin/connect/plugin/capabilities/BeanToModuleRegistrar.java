package com.atlassian.plugin.connect.plugin.capabilities;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilityModuleProvider;
import com.atlassian.plugin.spring.scanner.ProductFilter;
import com.atlassian.plugin.connect.plugin.capabilities.beans.CapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.CapabilityList;
import com.atlassian.plugin.connect.plugin.capabilities.beans.RemoteContainerCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.plugin.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.connect.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.AutowireWithConnectPluginDecorator;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.module.ContainerAccessor;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.osgi.factory.OsgiPlugin;
import com.atlassian.sal.api.ApplicationProperties;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.plugin.capabilities.util.ConnectReflectionHelper.isParameterizedListWithType;
import static com.google.common.collect.Lists.newArrayList;

@Component
public class BeanToModuleRegistrar
{
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

    public void registerDescriptorsForBeans(Plugin plugin, CapabilityList capabilityList)
    {
        BundleContext addonBundleContext = ((OsgiPlugin) plugin).getBundle().getBundleContext();
        AutowireWithConnectPluginDecorator connectAutowiringPlugin = new AutowireWithConnectPluginDecorator((AutowireCapablePlugin) theConnectPlugin, plugin, Sets.<Class<?>>newHashSet(productAccessor.getConditions().values()));
        List<DescriptorToRegister> descriptorsToRegister = new ArrayList<DescriptorToRegister>();

        BeanTransformContext ctx = new BeanTransformContext(connectAutowiringPlugin, addonBundleContext, ProductFilter.valueOf(applicationProperties.getDisplayName().toUpperCase()));

        //we MUST process the container bean first
        processContainer(capabilityList, ctx, descriptorsToRegister);

        //now process the other fields
        processFields(capabilityList, ctx, descriptorsToRegister);


        if (!descriptorsToRegister.isEmpty())
        {
            registrations.putIfAbsent(plugin.getKey(), dynamicDescriptorRegistration.registerDescriptors(plugin, descriptorsToRegister));
        }
    }

    private void processContainer(CapabilityList capabilityList, BeanTransformContext ctx, List<DescriptorToRegister> descriptorsToRegister)
    {
        RemoteContainerCapabilityBean connectContainer = capabilityList.getConnectContainer();
        if (null != connectContainer && !Strings.isNullOrEmpty(connectContainer.getDisplayUrl()))
        {
            try
            {

                //just to be safe, use reflection to pull the module provider
                Field containerField = capabilityList.getClass().getDeclaredField(RemoteContainerCapabilityBean.CONNECT_CONTAINER);
                CapabilityModuleProvider containerAnno = containerField.getAnnotation(CapabilityModuleProvider.class);
                if (null != containerAnno)
                {
                    descriptorsToRegister.addAll(getDescriptors(ctx, containerField.getName(), containerAnno, newArrayList(connectContainer)));
                }
            }
            catch (NoSuchFieldException e)
            {
                //ignore
            }
        }
    }

    private void processFields(CapabilityList capabilityList, BeanTransformContext ctx, List<DescriptorToRegister> descriptorsToRegister)
    {
        for (Field field : capabilityList.getClass().getDeclaredFields())
        {
            //ignore the container field since we already processed it
            if (RemoteContainerCapabilityBean.CONNECT_CONTAINER.equals(field.getName()))
            {
                continue;
            }

            if (field.isAnnotationPresent(CapabilityModuleProvider.class))
            {
                try
                {
                    CapabilityModuleProvider anno = field.getAnnotation(CapabilityModuleProvider.class);
                    field.setAccessible(true);

                    Type fieldType = field.getGenericType();

                    List<? extends CapabilityBean> beanList;

                    if (isParameterizedListWithType(fieldType, CapabilityBean.class))
                    {
                        beanList = (List<? extends CapabilityBean>) field.get(capabilityList);
                    }
                    else
                    {
                        beanList = (List<? extends CapabilityBean>) newArrayList((CapabilityBean) field.get(capabilityList));
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


    private List<DescriptorToRegister> getDescriptors(BeanTransformContext ctx, String jsonFieldName, CapabilityModuleProvider providerAnnotation, List<? extends CapabilityBean> beans)
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
            reg.unregister();

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
