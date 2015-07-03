package com.atlassian.plugin.connect.spi.module.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ModuleJson;
import com.google.gson.JsonObject;

import java.util.List;

public interface ConnectModuleProvider
{
    List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, Plugin plugin, List<JsonObject> modules);
    
    String getDescriptorKey();
    
    Class getBeanClass();
}