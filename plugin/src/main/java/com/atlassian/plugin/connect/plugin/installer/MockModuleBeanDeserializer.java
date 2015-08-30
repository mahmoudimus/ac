package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockModuleBeanDeserializer<T extends ModuleBean> implements JsonDeserializer<Map<String, Supplier<List<ModuleBean>>>>
{
    private final Class<T> beanClass;
    
    public MockModuleBeanDeserializer(Class<T> beanClass)
    {
        this.beanClass = beanClass;
    }
    
    
    @Override
    public Map<String, Supplier<List<ModuleBean>>> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
    {
        Type rawModuleListType = new TypeToken<Map<String, List<JsonElement>>>() {}.getType();
        Map<String, List<JsonElement>> rawModuleList = context.deserialize(json, rawModuleListType);

        Map<String, Supplier<List<ModuleBean>>> moduleBeanListSuppliers = new HashMap<>();
        for (Map.Entry<String, List<JsonElement>> rawModuleListEntry : rawModuleList.entrySet())
        {
            String descriptorKey = rawModuleListEntry.getKey();
            final List<JsonElement> rawModules = rawModuleListEntry.getValue();
            final Supplier<List<ModuleBean>> moduleBeanSupplier = new Supplier<List<ModuleBean>>()
            {
                @Override
                public List<ModuleBean> get()
                {
                    return validate(rawModules, beanClass);
                }
            };
            moduleBeanListSuppliers.put(descriptorKey, Suppliers.memoize(moduleBeanSupplier));
        }
        return moduleBeanListSuppliers;
    }
    
    

    public List<ModuleBean> validate(List<JsonElement> modules, Class<T> type)
    {
        List<ModuleBean> beans = new ArrayList<>();
        for(JsonElement module : modules)
        {
            T bean = new Gson().fromJson(module, type);
            beans.add(bean);
        }
        return beans;
    }
    
}
