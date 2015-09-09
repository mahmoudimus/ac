package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderModuleDescriptor;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.predicate.ModuleDescriptorOfClassPredicate;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Map<String, Supplier<List<ModuleBean>>> moduleBeanListSuppliers = new HashMap<>();
        
        for (Map.Entry<String, JsonElement> rawModule : json.getAsJsonObject().entrySet())
        {
            if (!moduleProviders.keySet().contains(rawModule.getKey()))
            {
                // TODO pass an i18n key here?
                throw new InvalidDescriptorException("Module type " + rawModule.getKey() + " listed in the descriptor is not valid.");
            }
            final ConnectModuleProvider moduleProvider = moduleProviders.get(rawModule.getKey());
            
            final List<JsonObject> modules = new ArrayList<>();
            if (rawModule.getValue().isJsonObject())
            {
                if (moduleProvider.multipleModulesAllowed())
                {
                    throw new InvalidDescriptorException("Modules of type " + rawModule.getKey() + "should be provided in a JSON array.");
                }
                modules.add(rawModule.getValue().getAsJsonObject());
            }
            else
            {
                JsonArray moduleArray = rawModule.getValue().getAsJsonArray();

                for (int i = 0; i < moduleArray.size(); i++)
                {
                    JsonObject module = moduleArray.get(i).getAsJsonObject();
                    modules.add(module);
                }
            }

            Supplier<List<ModuleBean>> moduleBeanSupplier = Suppliers.memoize(new Supplier<List<ModuleBean>>()
            {
                @Override
                public List<ModuleBean> get()
                {
                    return moduleProvider.validate(modules, moduleProvider.getBeanClass());
                }
            });
            moduleBeanListSuppliers.put(rawModule.getKey(), moduleBeanSupplier);
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
}
