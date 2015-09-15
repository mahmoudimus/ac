package com.atlassian.plugin.connect.spi.module.provider;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.modules.schema.DescriptorValidationResult;
import com.atlassian.plugin.connect.modules.schema.JsonDescriptorValidator;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractConnectModuleProvider<T> implements ConnectModuleProvider<T>
{
    @Override
    public List<T> validate(JsonElement rawModules, Class<T> type, Plugin plugin) throws ConnectModuleValidationException
    {

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
        validateAgainstSchema(rawModules, schema);
        
        Gson deserializer = ConnectModulesGsonFactory.getGson();
        List<T> beans = new ArrayList<>();
        if (rawModules.isJsonObject())
        {
            if (multipleModulesAllowed())
            {
                throw new ConnectModuleValidationException(getDescriptorKey(), "Modules should be provided in a JSON array.");
            }
            beans.add(deserializer.fromJson(rawModules, type));
        }
        else
        {
            JsonArray moduleArray = rawModules.getAsJsonArray();

            for (int i = 0; i < moduleArray.size(); i++)
            {
                JsonElement module = moduleArray.get(i);
                beans.add(deserializer.fromJson(module, type));
            }
        }

        return beans;
    }
    
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }
    
    protected void validateAgainstSchema(JsonElement rawModules, String schema) throws ConnectModuleSchemaValidationException
    {
        JsonDescriptorValidator jsonDescriptorValidator = new JsonDescriptorValidator();

        String modules = "{\"" + getDescriptorKey() + "\": " + rawModules.toString() + "}";

        DescriptorValidationResult result = jsonDescriptorValidator.validate(modules, schema);
        if (!result.isValid())
        {
            throw new ConnectModuleSchemaValidationException(getDescriptorKey(), result.getReportAsString());
        }
        
    }
    
}
