package com.atlassian.plugin.connect.spi.module.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ModuleJson;
import com.google.gson.JsonObject;

import java.util.List;

public abstract class ConnectModuleProvider
{
    public abstract List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin, List<JsonObject> modules);
    
    public abstract String getDescriptorKey();
    
    public abstract Class getBeanClass();
    
    public boolean validate(List<JsonObject> modules)
    {
        return true;
        
    }
}