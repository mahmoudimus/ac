package com.atlassian.plugin.connect.plugin.capabilities.module.serialise;

import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ModuleList;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProviderRegistry;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.DefaultModuleDeserialiser;
import com.atlassian.plugin.connect.spi.module.provider.Module;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Map;

public interface ConnectAddonBeanSerialiser
{
    ConnectAddonBean deserialise(String serialisedAddon);

    String serialise(ConnectAddonBean addon);
}





