package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderModuleDescriptor;
import com.atlassian.plugin.predicate.ModuleDescriptorOfClassPredicate;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PluginAwareModuleBeanDeserializer implements JsonDeserializer<Map<String, Supplier<List<ModuleBean>>>>
{
    private final Map<String, ConnectModuleProvider> moduleProviders;

    public PluginAwareModuleBeanDeserializer(PluginAccessor pluginAccessor)
    {
        this.moduleProviders = buildModuleProviderMap(pluginAccessor.getModules(new ModuleDescriptorOfClassPredicate<>(ConnectModuleProviderModuleDescriptor.class)));
    }

    @Override
    public Map<String, Supplier<List<ModuleBean>>> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
    {
        Type rawModuleListType = new TypeToken<Map<String, List<JsonObject>>>() {}.getType();
        Map<String, List<JsonObject>> rawModuleList = context.deserialize(json, rawModuleListType);
        
        for(Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet())
        {
            JsonArray moduleArray = entry.getValue().getAsJsonArray();
            List<JsonObject> modules = new ArrayList<>();
            for (int i = 0; i < moduleArray.size(); i++)
            {
                JsonObject module = moduleArray.get(i).getAsJsonObject();
                modules.add(module);
            }
            rawModuleList.put(entry.getKey(), modules);
        }
        
        assertAllModuleProvidersKnown(rawModuleList);

        Map<String, Supplier<List<ModuleBean>>> moduleBeanListSuppliers = new HashMap<>();
        for (Map.Entry<String, List<JsonObject>> rawModuleListEntry : rawModuleList.entrySet())
        {
            String descriptorKey = rawModuleListEntry.getKey();
            final List<JsonObject> rawModules = rawModuleListEntry.getValue();
            final ConnectModuleProvider moduleProvider = moduleProviders.get(descriptorKey);
            final Supplier<List<ModuleBean>> moduleBeanSupplier = new Supplier<List<ModuleBean>>()
            {
                @Override
                public List<ModuleBean> get()
                {
                    return moduleProvider.validate(rawModules, moduleProvider.getBeanClass());
                }
            };
            moduleBeanListSuppliers.put(descriptorKey, Suppliers.memoize(moduleBeanSupplier));
        }

        return moduleBeanListSuppliers;
    }

    private Map<String, ConnectModuleProvider> buildModuleProviderMap(Collection<ConnectModuleProvider> moduleProviders)
    {
        Map<String, ConnectModuleProvider> moduleProviderMap = new HashMap<>();
        for (ConnectModuleProvider moduleProvider : moduleProviders)
        {
            moduleProviderMap.put(moduleProvider.getDescriptorKey(), moduleProvider);
        }
        return moduleProviderMap;
    }

    private void assertAllModuleProvidersKnown(Map<String, List<JsonObject>> rawModuleList)
    {
        final Set<String> unknownModuleTypes = Sets.difference(rawModuleList.keySet(), moduleProviders.keySet());
        if (!unknownModuleTypes.isEmpty())
        {
            // Unknown module providers
        }
    }
}
