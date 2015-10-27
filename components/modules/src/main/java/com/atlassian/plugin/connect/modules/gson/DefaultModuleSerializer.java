package com.atlassian.plugin.connect.modules.gson;

import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.google.common.base.Supplier;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultModuleSerializer implements JsonSerializer<Map<String, Supplier<List<ModuleBean>>>>
{
    @Override
    public JsonElement serialize(Map<String, Supplier<List<ModuleBean>>> src, Type typeOfSrc, final JsonSerializationContext context)
    {
        Map<String, List<ModuleBean>> jsonMap = new HashMap<>();
        for (Map.Entry<String,Supplier<List<ModuleBean>>> entry : src.entrySet())
        {
            List<ModuleBean> moduleBeans = entry.getValue().get();
            jsonMap.put(entry.getKey(), moduleBeans);
        }
        return context.serialize(jsonMap);
    }
}