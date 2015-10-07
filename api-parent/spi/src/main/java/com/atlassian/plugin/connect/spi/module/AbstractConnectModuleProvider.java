package com.atlassian.plugin.connect.spi.module;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.modules.schema.DescriptorValidationResult;
import com.atlassian.plugin.connect.modules.schema.JsonDescriptorValidator;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractConnectModuleProvider<T extends BaseModuleBean> implements ConnectModuleProvider<T>
{
    @Override
    public List<T> deserializeAddonDescriptorModules(String jsonModuleListEntry, Plugin plugin, ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException
    {
        validateAgainstSchema(jsonModuleListEntry, plugin, descriptor);
        return deserializeAddonDescriptorModules(jsonModuleListEntry);
    }
    
    protected List<T> deserializeAddonDescriptorModules(String jsonModuleListEntry) throws ConnectModuleValidationException
    {
        JsonElement modulesElement = new JsonParser().parse(jsonModuleListEntry);
        Gson gson = ConnectModulesGsonFactory.getGson();
        List<T> beans;
        if (modulesElement.isJsonObject())
        {
            assertMultipleModulesNotAllowed();
            T module = gson.fromJson(jsonModuleListEntry, getMeta().getBeanClass());
            beans = Collections.singletonList(module);
        }
        else
        {
            assertMultipleModulesAllowed();
            beans = new ArrayList<>();
            for (JsonElement moduleElement : modulesElement.getAsJsonArray())
            {
                beans.add(gson.fromJson(moduleElement, getMeta().getBeanClass()));
            }
        }
        return beans;
    }

    protected void validateAgainstSchema(String rawModules, Plugin plugin, ShallowConnectAddonBean bean) throws ConnectModuleSchemaValidationException
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

    private void assertMultipleModulesAllowed() throws ConnectModuleValidationException
    {
        if (getMeta().multipleModulesAllowed())
        {
            throw new ConnectModuleValidationException(getMeta().getDescriptorKey(), "Modules should be provided in a JSON array.");
        }
    }

    private void assertMultipleModulesNotAllowed() throws ConnectModuleValidationException
    {
        if (!getMeta().multipleModulesAllowed())
        {
            throw new ConnectModuleValidationException(getMeta().getDescriptorKey(), "Modules should be provided in a JSON array.");
        }
    }
}
