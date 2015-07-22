package com.atlassian.plugin.connect.modules.gson;

import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultModuleDeserializer implements JsonDeserializer<Map<String, Supplier<List<ModuleBean>>>>
{
    @Override
    public Map<String, Supplier<List<ModuleBean>>> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException
    {
        Type rawModuleListType = new TypeToken<Map<String, List<JsonObject>>>() {}.getType();
        Map<String, List<JsonObject>> rawModuleList = new Gson().fromJson(json, rawModuleListType);

        Map<String, Supplier<List<ModuleBean>>> moduleBeanListSuppliers = new HashMap<>();
        for (Map.Entry<String, List<JsonObject>> rawModuleListEntry : rawModuleList.entrySet())
        {
            String descriptorKey = rawModuleListEntry.getKey();
            final List<JsonObject> rawModules = rawModuleListEntry.getValue();
            Supplier<List<ModuleBean>> moduleBeanSupplier = new Supplier<List<ModuleBean>>()
            {
                @Override
                public List<ModuleBean> get()
                {
                    List<ModuleBean> moduleBeans = new ArrayList<>();
                    for(JsonObject module : rawModules)
                    {
                        ModuleBean bean = new Gson().fromJson(module, ModuleBean.class);
                        moduleBeans.add(bean);
                    }
                    return moduleBeans;
                }
            };
            moduleBeanListSuppliers.put(descriptorKey, Suppliers.memoize(moduleBeanSupplier));
        }

        return moduleBeanListSuppliers;

    }

}