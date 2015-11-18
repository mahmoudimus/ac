package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@VisibleForTesting
public class StaticModuleListDeserializer extends ModuleListDeserializer
{
    private final Set<ConnectModuleMeta> moduleMetas;

    public StaticModuleListDeserializer(ShallowConnectAddonBean addon, ConnectModuleMeta... moduleMetas)
    {
        super(addon);
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
    public List<ModuleBean> deserializeModules(String moduleTypeKey, JsonElement modules) throws ConnectModuleValidationException
    {
        ConnectModuleMeta moduleMeta = getModuleMeta(moduleTypeKey);
        if (moduleMeta == null)
        {
            throwUnknownModuleType(moduleTypeKey);
        }

        Gson deserializer = ConnectModulesGsonFactory.getGson();
        List<ModuleBean> beans = new ArrayList<>();
        Class<? extends ModuleBean> beanClass = moduleMeta.getBeanClass();
        if (modules.isJsonObject())
        {
            beans.add(deserializer.fromJson(modules, beanClass));
        }
        else
        {
            JsonArray moduleArray = modules.getAsJsonArray();
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
    public boolean multipleModulesAllowed(String moduleType)
    {
        return getModuleMeta(moduleType).multipleModulesAllowed();
    }
}
