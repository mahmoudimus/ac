package com.atlassian.plugin.connect.modules.gson;

import com.atlassian.plugin.connect.modules.beans.BaseModuleList;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class ModuleListGroupSerialiser implements JsonSerializer<ModuleListGroup>
{
    @Override
    public JsonElement serialize(ModuleListGroup moduleLists, Type typeOfSrc,
                                 JsonSerializationContext context)
    {
        if (moduleLists == null || moduleLists.getModuleLists() == null)
        {
            return null;
        }

        JsonObject result = new JsonObject();
        for (BaseModuleList moduleList : moduleLists.getModuleLists().values())
        {
            JsonElement jsonElement = context.serialize(moduleList);
            if (!jsonElement.isJsonObject())
            {
                throw new IllegalStateException("Deserialised a module list into something other than an object!! - " + jsonElement);
            }

            for (Map.Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().entrySet())
            {
                result.add(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }
}

class ModuleListGroup
{
    private Map<Class<? extends BaseModuleList>, BaseModuleList> moduleLists = newHashMap();

    public Map<Class<? extends BaseModuleList>, BaseModuleList> getModuleLists()
    {
        return moduleLists;
    }

}
