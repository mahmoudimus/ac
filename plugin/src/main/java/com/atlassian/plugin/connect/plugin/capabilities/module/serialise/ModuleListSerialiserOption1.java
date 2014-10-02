package com.atlassian.plugin.connect.plugin.capabilities.module.serialise;

import com.atlassian.plugin.connect.modules.beans.ModuleList;
import com.atlassian.plugin.connect.plugin.capabilities.provider.blah.ConnectModuleProviderRegistry;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.DefaultModuleDeserialiser;
import com.atlassian.plugin.connect.spi.module.provider.Module;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

public class ModuleListSerialiserOption1 implements ModuleListSerialiser
{
    private final ConnectModuleProviderRegistry registry;

    public ModuleListSerialiserOption1(ConnectModuleProviderRegistry registry)
    {
        this.registry = registry;
    }

    @Override
    public ModuleList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        if (!json.isJsonObject())
        {
            throw new JsonParseException("modules must be an object");
        }

        // damn can't do this with gson
//        ModuleList moduleList = context.deserialize(json, ModuleList.class);
        ModuleList moduleList = new ModuleList();

        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet())
        {
            ConnectModuleProvider<?> provider = registry.get(entry.getKey());
            if (provider != null)
            {
                List<Object> modules = newArrayList();
                for (JsonElement jsonElement : entry.getValue().getAsJsonArray())
                {
//                    modules.add(new ModuleImpl(jsonElement.getAsJsonObject(), context));
                    Object bean =  provider.getDeserialiserProvider().deserialise(new DefaultModuleDeserialiser(context, json));
                    modules.add(bean);
                }

                moduleList.getModules().put(entry.getKey(), modules);
            }

        }

        return moduleList;
    }

    @Override
    public JsonElement serialize(ModuleList moduleList, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonElement legacyModules = context.serialize(moduleList);
        JsonElement coolKids = context.serialize(moduleList.getModules());

        JsonObject combinedModuleJson = coolKids.getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry :  legacyModules.getAsJsonObject().entrySet())
        {
            combinedModuleJson.add(entry.getKey(), entry.getValue());
        }

        return combinedModuleJson;
    }
}
