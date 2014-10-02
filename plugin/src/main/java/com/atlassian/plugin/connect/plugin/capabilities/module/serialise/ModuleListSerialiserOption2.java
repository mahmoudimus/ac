package com.atlassian.plugin.connect.plugin.capabilities.module.serialise;

import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleList;
import com.atlassian.plugin.connect.spi.module.provider.Module;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Map;

public class ModuleListSerialiserOption2 implements ModuleListSerialiser
{
    @Override
    public ModuleList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        if (!json.isJsonObject())
        {
            throw new JsonParseException("modules must be an object");
        }

        ModuleList moduleList = context.deserialize(json, ModuleList.class);

        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet())
        {
            ModuleImpl module = new ModuleImpl(entry.getValue().getAsJsonObject(), context);
            moduleList.getModules().put(entry.getKey(), module);
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

    private static class ModuleImpl implements Module
    {

        private final JsonObject json;
        private final JsonDeserializationContext context;

        public ModuleImpl(JsonObject json, JsonDeserializationContext context)
        {
            this.json = json;
            this.context = context;
        }

        @Override
        public BaseModuleBean toBean(Class<? extends BaseModuleBean> moduleClass)
        {
            return context.deserialize(json, moduleClass);
        }
    }
}
