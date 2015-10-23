package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.spi.module.ConnectModuleValidationException;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleListDeserializer implements JsonDeserializer<Map<String, Supplier<List<ModuleBean>>>>, JsonSerializer<Map<String, Supplier<List<ModuleBean>>>>
{
    private AvailableModuleTypes providers;
    
    public ModuleListDeserializer(AvailableModuleTypes providers)
    {
        this.providers = providers;
    }
    
    @Override
    public Map<String, Supplier<List<ModuleBean>>> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
    {
        Map<String, Supplier<List<ModuleBean>>> moduleBeanListSuppliers = new HashMap<>();

        for (final Map.Entry<String, JsonElement> rawModuleEntry : json.getAsJsonObject().entrySet())
        {
            String descriptorKey = rawModuleEntry.getKey();
            assertValidModuleType(descriptorKey);
            moduleBeanListSuppliers.put(descriptorKey, createModuleBeanListSupplier(rawModuleEntry));
        }

        return moduleBeanListSuppliers;
    }

    @Override
    public JsonElement serialize(Map<String, Supplier<List<ModuleBean>>> src, Type typeOfSrc, final JsonSerializationContext context)
    {
        JsonObject object = new JsonObject();
        for (Map.Entry<String,Supplier<List<ModuleBean>>> entry : src.entrySet())
        {
            List<ModuleBean> moduleBeans = entry.getValue().get();
            JsonElement element;
            if (providers.multipleModulesAllowed(entry.getKey()))
            {
                element = context.serialize(moduleBeans);
            }
            else
            {
                element = context.serialize(moduleBeans.get(0));
            }
            object.add(entry.getKey(), element);
        }
        return object;
    }

    private void assertValidModuleType(String descriptorKey)
    {
        if (!providers.validModuleType(descriptorKey))
        {
            throw new InvalidDescriptorException("No provider found for module type " + descriptorKey + " referenced in the descriptor",
                    "connect.install.error.unknown.module", descriptorKey);
        }
    }

    private Supplier<List<ModuleBean>> createModuleBeanListSupplier(Map.Entry<String, JsonElement> rawModuleEntry)
    {
        return Suppliers.memoize(() -> {
            try
            {
                return providers.deserializeModulesOfSameType(rawModuleEntry);
            } catch (ConnectModuleValidationException e)
            {
                throw new ConnectModuleValidationRuntimeException(e);
            }
        });
    }
}
