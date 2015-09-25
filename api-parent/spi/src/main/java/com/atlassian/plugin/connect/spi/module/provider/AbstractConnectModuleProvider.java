package com.atlassian.plugin.connect.spi.module.provider;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.modules.schema.DescriptorValidationResult;
import com.atlassian.plugin.connect.modules.schema.JsonDescriptorValidator;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractConnectModuleProvider<T> implements ConnectModuleProvider<T>
{
    @Override
    public List<T> validate(String rawModules, Class<T> type, Plugin plugin) throws ConnectModuleValidationException
    {
        validateAgainstSchema(rawModules, plugin);
        return deserializeIntoBeans(rawModules, type);
    }
    
    protected List<T> deserializeIntoBeans(String rawModules, Class<T> type) throws ConnectModuleValidationException
    {
        JsonElement modulesElement = new JsonParser().parse(rawModules);
        Gson deserializer = ConnectModulesGsonFactory.getGson();
        List<T> beans = new ArrayList<>();
        if (modulesElement.isJsonObject())
        {
            if (getMeta().multipleModulesAllowed())
            {
                throw new ConnectModuleValidationException(getMeta().getDescriptorKey(), "Modules should be provided in a JSON array.");
            }
            beans.add(deserializer.fromJson(rawModules, type));
        }
        else
        {
            JsonArray moduleArray = modulesElement.getAsJsonArray();

            for (int i = 0; i < moduleArray.size(); i++)
            {
                JsonElement module = moduleArray.get(i);
                beans.add(deserializer.fromJson(module, type));
            }
        }
        return beans;
    }
    
    protected void validateAgainstSchema(String rawModules, Plugin plugin) throws ConnectModuleSchemaValidationException
    {
        if (getSchemaPrefix() == null)
        {
            return;
        }

        final String schema;
        try
        {
            InputStream in = plugin.getResourceAsStream("/schema/" + getSchemaPrefix() + "-schema.json");
            schema = IOUtils.toString(in);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Failed to read JSON schema for descriptor", e);
        }
        
        JsonDescriptorValidator jsonDescriptorValidator = new JsonDescriptorValidator();

        String modules = "{\"" + getMeta().getDescriptorKey() + "\": " + rawModules + "}";

        DescriptorValidationResult result = jsonDescriptorValidator.validate(modules, schema);
        if (!result.isValid())
        {
            throw new ConnectModuleSchemaValidationException(getMeta().getDescriptorKey(), result.getReportAsString(), modules);
        }
        
    }
    
}
