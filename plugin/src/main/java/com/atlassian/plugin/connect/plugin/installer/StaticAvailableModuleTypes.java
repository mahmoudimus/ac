package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StaticAvailableModuleTypes implements AvailableModuleTypes
{    
    private final Set<ConnectModuleMeta> moduleMetas;
    
    public StaticAvailableModuleTypes(ConnectModuleMeta... moduleMetas)
    {
        this.moduleMetas = new HashSet<>(Arrays.asList(moduleMetas));
    }
    
    public void addModuleMeta(ConnectModuleMeta moduleMeta)
    {
        moduleMetas.add(moduleMeta);
    }
    
    public boolean hasMetas()
    {
        return !moduleMetas.isEmpty();
    }

    @Override
    public List<ModuleBean> deserializeModulesOfSameType(Map.Entry<String, JsonElement> modules)
    {
        Gson deserializer = ConnectModulesGsonFactory.getGson();
        List<ModuleBean> beans = new ArrayList<>();
        Class<? extends ModuleBean> beanClass = getModuleMeta(modules.getKey()).getBeanClass();
        if (modules.getValue().isJsonObject())
        {
            beans.add(deserializer.fromJson(modules.getValue(), beanClass));
        }
        else
        {
            JsonArray moduleArray = modules.getValue().getAsJsonArray();

            for (int i = 0; i < moduleArray.size(); i++)
            {
                JsonElement module = moduleArray.get(i);
                beans.add(deserializer.fromJson(module, beanClass));
            }
        }
        return beans;
        
    }
    
    public ConnectModuleMeta getModuleMeta(String type)
    {
        for (ConnectModuleMeta moduleMeta : moduleMetas)
        {
            if (moduleMeta.getDescriptorKey().equals(type))
            {
                return moduleMeta;
            }
        }
        return null;        
    }

    @Override
    public boolean validModuleType(String moduleType)
    {
        return getModuleMeta(moduleType) != null;
    }
    
    @Override
    public boolean multipleModulesAllowed(String moduleType)
    {
        return getModuleMeta(moduleType).multipleModulesAllowed();
    }
}
