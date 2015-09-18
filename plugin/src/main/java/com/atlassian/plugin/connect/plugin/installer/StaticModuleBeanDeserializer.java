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

public class StaticModuleBeanDeserializer extends ModuleBeanDeserializer
{    
    private final Set<ConnectModuleMeta> moduleMetas;
    
    public StaticModuleBeanDeserializer(ConnectModuleMeta... moduleMetas)
    {
        this.moduleMetas = new HashSet<>(Arrays.asList(moduleMetas));
    }
    
    public void addModuleMeta(ConnectModuleMeta moduleMeta)
    {
        moduleMetas.add(moduleMeta);
    }

    @Override
    protected List<ModuleBean> deserializeModulesOfSameType(Map.Entry<String, JsonElement> modules)
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
    
    private ConnectModuleMeta getModuleMeta(String type)
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
    protected boolean validModuleType(String moduleType)
    {
        return getModuleMeta(moduleType) != null;
    }
    
    @Override
    protected boolean multipleModulesAllowed(String moduleType)
    {
        return getModuleMeta(moduleType).multipleModulesAllowed();
    }
}
