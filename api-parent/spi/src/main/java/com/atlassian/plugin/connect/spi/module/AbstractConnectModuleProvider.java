package com.atlassian.plugin.connect.spi.module;

import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidationResult;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractConnectModuleProvider<T extends BaseModuleBean> implements ConnectModuleProvider<T>
{

    @Override
    public List<T> deserializeAddonDescriptorModules(String jsonModuleListEntry, ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException
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

    protected void assertDescriptorValidatesAgainstSchema(String jsonModuleListEntry, URL schemaUrl,
            ConnectJsonSchemaValidator schemaValidator) throws ConnectModuleSchemaValidationException
    {
        String modules = String.format("{\"%s\": %s}", getMeta().getDescriptorKey(), jsonModuleListEntry);
        ConnectJsonSchemaValidationResult result = schemaValidator.validate(modules, schemaUrl);
        if (!result.isValid())
        {
            throw new ConnectModuleSchemaValidationException(getMeta().getDescriptorKey(), result.getReportAsString(), modules);
        }
    }

    private void assertMultipleModulesAllowed() throws ConnectModuleValidationException
    {
        if (!getMeta().multipleModulesAllowed())
        {
            throw new ConnectModuleValidationException(getMeta().getDescriptorKey(), "Modules should be provided as a JSON array of objects.");
        }
    }

    private void assertMultipleModulesNotAllowed() throws ConnectModuleValidationException
    {
        if (getMeta().multipleModulesAllowed())
        {
            throw new ConnectModuleValidationException(getMeta().getDescriptorKey(), "A single module should be provided as a JSON object.");
        }
    }
}
