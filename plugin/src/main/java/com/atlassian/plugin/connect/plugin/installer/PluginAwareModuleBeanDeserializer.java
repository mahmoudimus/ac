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
        
        for (final Map.Entry<String, JsonElement> rawModuleEntry : json.getAsJsonObject().entrySet())
        {
            if (!moduleProviders.keySet().contains(rawModuleEntry.getKey()))
            {
                // TODO pass an i18n key here?
                throw new InvalidDescriptorException("Module type " + rawModuleEntry.getKey() + " listed in the descriptor is not valid.");
            }
            final ConnectModuleProvider moduleProvider = moduleProviders.get(rawModuleEntry.getKey());

            Supplier<List<ModuleBean>> moduleBeanSupplier = Suppliers.memoize(new Supplier<List<ModuleBean>>()
            {
                @Override
                public List<ModuleBean> get()
                {
                    try
                    {
                        return moduleProvider.validate(rawModuleEntry.getValue(), moduleProvider.getBeanClass());
                    }
                    catch (Exception e)
                    {
                        throw new ModuleDeserializationException(e.getMessage());
                    }
                }
            });
            moduleBeanListSuppliers.put(rawModuleEntry.getKey(), moduleBeanSupplier);
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
