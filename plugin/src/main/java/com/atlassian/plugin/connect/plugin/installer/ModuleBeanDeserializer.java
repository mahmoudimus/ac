package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleValidationException;
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

public abstract class ModuleBeanDeserializer implements JsonDeserializer<Map<String, Supplier<List<ModuleBean>>>>, JsonSerializer<Map<String, Supplier<List<ModuleBean>>>>
{

    @Override
    public Map<String, Supplier<List<ModuleBean>>> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
    {
        Map<String, Supplier<List<ModuleBean>>> moduleBeanListSuppliers = new HashMap<>();

        for (final Map.Entry<String, JsonElement> rawModuleEntry : json.getAsJsonObject().entrySet())
        {
            if (!validModuleType(rawModuleEntry.getKey()))
            {
                // TODO pass an i18n key here?
                throw new InvalidDescriptorException("Module type " + rawModuleEntry.getKey() + " listed in the descriptor is not valid.");
            }

            Supplier<List<ModuleBean>> moduleBeanSupplier = Suppliers.memoize(new Supplier<List<ModuleBean>>()
            {
                @Override
                public List<ModuleBean> get()
                {
                    try
                    {
                        return deserializeModulesOfSameType(rawModuleEntry);
                    }
                    catch (ConnectModuleValidationException e)
                    {
                        throw new ModuleDeserializationException(e.getMessage());
                    }
                }
            });
            moduleBeanListSuppliers.put(rawModuleEntry.getKey(), moduleBeanSupplier);
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
            if (multipleModulesAllowed(entry.getKey()))
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
    
    protected abstract boolean validModuleType(String moduleType);
    
    protected abstract boolean multipleModulesAllowed(String moduleType);
    
    protected abstract List<ModuleBean> deserializeModulesOfSameType(Map.Entry<String, JsonElement> modules) throws ConnectModuleValidationException;
}
