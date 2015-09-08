package com.atlassian.plugin.connect.spi.module.provider;

import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractConnectModuleProvider<T> implements ConnectModuleProvider<T>
{

    @Override
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
