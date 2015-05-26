package com.atlassian.plugin.connect.plugin.capabilities.util;

import com.atlassian.plugin.connect.api.capabilities.util.ConnectContainerUtil;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultConnectContainerUtil implements ConnectContainerUtil
{
    private final ContainerManagedPlugin theConnectPlugin;

    @Autowired
    public DefaultConnectContainerUtil(PluginRetrievalService pluginRetrievalService)
    {
        this.theConnectPlugin = (ContainerManagedPlugin)pluginRetrievalService.getPlugin();
    }

    @Override
    public <T> T createBean(Class<T> clazz)
    {
        return theConnectPlugin.getContainerAccessor().createBean(clazz);
    }

    @Override
    public <T> Iterable<T> getBeansOfType(final Class<T> clazz)
    {
        return theConnectPlugin.getContainerAccessor().getBeansOfType(clazz);
    }
}
