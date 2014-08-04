package com.atlassian.plugin.connect.plugin.module.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.modules.annotation.ConnectModule;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.util.ProductFilter;
import com.atlassian.plugin.connect.spi.plugin.capabilities.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.plugin.capabilities.provider.DefaultConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.exception.ModuleProviderNotFoundException;
import com.atlassian.plugin.connect.plugin.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.module.ContainerAccessor;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.isParameterizedListWithType;
import static com.google.common.collect.Lists.newArrayList;

public class ModuleListProviderFactoryImpl implements ModuleListProviderFactory
{
    private final Object moduleProvider;
    private final ContainerManagedPlugin plugin;

    public ModuleListProviderFactoryImpl(final Object moduleProvider, final ContainerManagedPlugin plugin)
    {
        this.moduleProvider = moduleProvider;
        this.plugin = plugin;
    }

    @Override
    public List<DescriptorToRegister> getDescriptors(final ConnectAddonBean addon, final BeanTransformContext ctx)
    {
        final ImmutableList.Builder<DescriptorToRegister> builder = ImmutableList.builder();
        for (Field field : moduleProvider.getClass().getDeclaredFields())
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
                        beanList = (List<? extends ModuleBean>) field.get(moduleProvider);
                    }
                    else
                    {
                        ModuleBean moduleBean = (ModuleBean) field.get(moduleProvider);
                        beanList = moduleBean == null ? ImmutableList.<ModuleBean>of() : newArrayList(moduleBean);
                    }

                    builder.addAll(getDescriptors(addon, ctx, field.getName(), anno, beanList));
                }
                catch (IllegalAccessException e)
                {
                    //ignore. this should never happen
                }
            }
        }
        return builder.build();
    }

    private List<DescriptorToRegister> getDescriptors(ConnectAddonBean addon, BeanTransformContext ctx,
            String jsonFieldName, ConnectModule providerAnnotation, List<? extends ModuleBean> beans)
    {
        List<ProductFilter> products = Arrays.asList(providerAnnotation.products());
        if (products.contains(ProductFilter.ALL) || (null != ctx.getAppFilter() && products.contains(ctx.getAppFilter())))
        {
            Class<? extends ConnectModuleProvider> theProviderClass = null;
            try
            {
                theProviderClass = (Class<? extends ConnectModuleProvider>) plugin.getClassLoader().loadClass(providerAnnotation.value());
            }
            catch (ClassNotFoundException e)
            {
                throw new ModuleProviderNotFoundException("Unable to load module provider for class [" + providerAnnotation.value() + "]", e);
            }

            ContainerAccessor accessor = plugin.getContainerAccessor();
            Collection<? extends ConnectModuleProvider> providers = accessor.getBeansOfType(theProviderClass);

            if (!providers.isEmpty())
            {
                ConnectModuleProvider provider = providers.iterator().next();

                List<ModuleDescriptor> descriptors = provider.provideModules(
                        new DefaultConnectModuleProviderContext(addon), ctx.getTheConnectPlugin(), jsonFieldName, beans);

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

}
