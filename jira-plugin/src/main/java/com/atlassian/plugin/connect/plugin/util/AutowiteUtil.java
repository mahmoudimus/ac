package com.atlassian.plugin.connect.plugin.util;

import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.springframework.stereotype.Component;

@Component
public class AutowiteUtil
{
    private final ContainerManagedPlugin containerManagedPlugin;

    public AutowiteUtil(final PluginRetrievalService pluginRetrievalService)
    {
        this.containerManagedPlugin = (ContainerManagedPlugin) pluginRetrievalService.getPlugin();
    }

    public <T> T createBean(Class<T> clazz)
    {
        return containerManagedPlugin.getContainerAccessor().createBean(clazz);
    }
}
