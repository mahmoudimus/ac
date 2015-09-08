package com.atlassian.plugin.connect.spi.module.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.google.gson.JsonObject;

import java.util.List;

public interface ConnectModuleProvider<T>
{

    String getDescriptorKey();

    Class getBeanClass();

    List<T> validate(List<JsonObject> modules, Class<T> type);

    List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, Plugin plugin, List<T> beans);
}
