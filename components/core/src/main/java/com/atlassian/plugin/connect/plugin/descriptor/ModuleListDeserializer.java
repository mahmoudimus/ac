package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
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
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ModuleListDeserializer implements JsonDeserializer<Map<String, Supplier<List<ModuleBean>>>>, JsonSerializer<Map<String, Supplier<List<ModuleBean>>>> {

    protected ShallowConnectAddonBean addon;

    public ModuleListDeserializer(ShallowConnectAddonBean addon) {
        this.addon = addon;
    }

    @Override
    public Map<String, Supplier<List<ModuleBean>>> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        return json.getAsJsonObject().entrySet().stream()
                .map((entry) -> {
                    String descriptorKey = entry.getKey();
                    return new AbstractMap.SimpleEntry<>(descriptorKey, createModuleBeanListSupplier(descriptorKey, entry.getValue()));
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public JsonElement serialize(Map<String, Supplier<List<ModuleBean>>> src, Type typeOfSrc, final JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        for (Map.Entry<String, Supplier<List<ModuleBean>>> entry : src.entrySet()) {
            List<ModuleBean> moduleBeans = entry.getValue().get();
            JsonElement element;
            if (multipleModulesAllowed(entry.getKey())) {
                element = context.serialize(moduleBeans);
            } else {
                element = context.serialize(moduleBeans.get(0));
            }
            object.add(entry.getKey(), element);
        }
        return object;
    }

    protected abstract boolean multipleModulesAllowed(String moduleType);

    protected abstract List<ModuleBean> deserializeModules(String moduleTypeKey, JsonElement modules) throws ConnectModuleValidationException;

    protected void throwUnknownModuleType(String moduleTypeKey) throws ConnectModuleValidationException {
        throw new ConnectModuleValidationException(
                addon,
                new ConnectModuleMeta(moduleTypeKey, ModuleBean.class) {
                },
                "No provider found for module type " + moduleTypeKey + " referenced in the descriptor",
                "connect.install.error.unknown.module",
                moduleTypeKey);
    }

    private Supplier<List<ModuleBean>> createModuleBeanListSupplier(String moduleTypeKey, JsonElement modules) {
        return Suppliers.memoize(() -> {
            try {
                return deserializeModules(moduleTypeKey, modules);
            } catch (ConnectModuleValidationException e) {
                throw new ConnectModuleValidationRuntimeException(e);
            }
        });
    }
}
