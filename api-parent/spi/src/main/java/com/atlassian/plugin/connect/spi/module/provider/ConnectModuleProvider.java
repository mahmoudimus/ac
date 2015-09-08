package com.atlassian.plugin.connect.spi.module.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public abstract class ConnectModuleProvider<T>
{
    public abstract List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin, List<T> beans);
    
    public abstract String getDescriptorKey();

    public abstract Class getBeanClass();
    
    public List<T> validate(List<JsonObject> modules, Class<T> type)
    {
        List<T> beans = new ArrayList<>();
        for(JsonObject module : modules)
        {
            T bean = ConnectModulesGsonFactory.getGson().fromJson(module, type);
            beans.add(bean);
        }
        return beans;
    }
}