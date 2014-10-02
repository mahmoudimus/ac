package com.atlassian.plugin.connect.plugin.capabilities.module.serialise;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ModuleList;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ConnectAddonBeanGsonSerialiser implements ConnectAddonBeanSerialiser
{
    private final Gson gson;

    public ConnectAddonBeanGsonSerialiser(ModuleListSerialiser moduleListSerialiser)
    {
        GsonBuilder gsonBuilder = ConnectModulesGsonFactory.getGsonBuilder();
        gsonBuilder.registerTypeAdapter(ModuleList.class, moduleListSerialiser);
        gson = gsonBuilder.create();
    }

    @Override
    public ConnectAddonBean deserialise(String serialisedAddon)
    {
        return gson.fromJson(serialisedAddon, ConnectAddonBean.class);
    }

    @Override
    public String serialise(ConnectAddonBean addon)
    {
        return gson.toJson(addon);
    }
}
