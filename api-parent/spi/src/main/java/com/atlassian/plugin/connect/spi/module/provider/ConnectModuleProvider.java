package com.atlassian.plugin.connect.spi.module.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.google.gson.JsonElement;

import java.util.List;

public interface ConnectModuleProvider<T>
{
    String getSchemaPrefix();
    
    ConnectModuleMeta getMeta();

    List<T> validate(JsonElement modules, Class<T> type, Plugin plugin) throws ConnectModuleValidationException;

    List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, Plugin plugin, List<T> beans);
}