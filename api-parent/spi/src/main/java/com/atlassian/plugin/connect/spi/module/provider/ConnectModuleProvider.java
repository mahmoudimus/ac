package com.atlassian.plugin.connect.spi.module.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.google.gson.JsonElement;

import java.util.List;

public interface ConnectModuleProvider<T>
{
    boolean multipleModulesAllowed();

    String getDescriptorKey();

    Class getBeanClass();

    List<T> validate(JsonElement modules, Class<T> type) throws Exception;

    List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, Plugin plugin, List<T> beans);
}
