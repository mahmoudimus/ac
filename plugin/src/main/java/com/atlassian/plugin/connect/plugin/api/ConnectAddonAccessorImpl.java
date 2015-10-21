package com.atlassian.plugin.connect.plugin.api;

import com.atlassian.plugin.connect.api.ConnectAddonAccessor;
import com.atlassian.plugin.connect.api.installer.AddonSettings;
import com.atlassian.plugin.connect.api.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.BeanToModuleRegistrar;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonBeanFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Optional;

@Component
@ExportAsService
public class ConnectAddonAccessorImpl implements ConnectAddonAccessor
{

    private final ConnectAddonRegistry addonRegistry;
    private final ConnectAddonBeanFactory addonBeanFactory;
    private final BeanToModuleRegistrar beanToModuleRegistrar;

    @Inject
    public ConnectAddonAccessorImpl(ConnectAddonRegistry addonRegistry,
            ConnectAddonBeanFactory connectAddonBeanFactory,
            BeanToModuleRegistrar beanToModuleRegistrar)
    {
        this.addonRegistry = addonRegistry;
        this.addonBeanFactory = connectAddonBeanFactory;
        this.beanToModuleRegistrar = beanToModuleRegistrar;
    }

    @Override
    public boolean isAddonEnabled(final String addonKey)
    {
        return beanToModuleRegistrar.descriptorsAreRegistered(addonKey);
    }

    @Override
    public Optional<ConnectAddonBean> getAddon(String addonKey)
    {
        return getAddonForDescriptor(addonRegistry.getDescriptor(addonKey));
    }

    @Override
    public Collection<ConnectAddonBean> getAllAddons()
    {
        ImmutableList.Builder<ConnectAddonBean> addonsBuilder = ImmutableList.builder();
        for (AddonSettings addonSettings : addonRegistry.getAllAddonSettings())
        {
            Optional<ConnectAddonBean> optionalAddon = getAddonForDescriptor(addonSettings.getDescriptor());
            if (optionalAddon.isPresent())
            {
                addonsBuilder.add(optionalAddon.get());
            }
        }
        return addonsBuilder.build();
    }

    private Optional<ConnectAddonBean> getAddonForDescriptor(@Nullable String descriptor)
    {
        Optional<ConnectAddonBean> optionalAddon = Optional.empty();
        if (!Strings.isNullOrEmpty(descriptor))
        {
            optionalAddon = Optional.of(addonBeanFactory.fromJson(descriptor));
        }
        return optionalAddon;
    }
}
