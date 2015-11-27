package com.atlassian.plugin.connect.spi.lifecycle;

import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidationException;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.descriptor.ConnectModuleSchemaValidationException;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A base class for providers of feature modules for Atlassian Connect.
 *
 * @param <T> the type of the add-on descriptor module representation
 */
public abstract class AbstractConnectModuleProvider<T extends BaseModuleBean> implements ConnectModuleProvider<T>
{

    /**
     * Deserializes the given JSON module list entry as a JSON object or array of JSON objects based on the metadata of
     * the module type.
     *
     * @param jsonModuleListEntry the string representation of the module list entry JSON element
     * @param descriptor the add-on descriptor (without the module list)
     * @return the module beans deserialized from the module list entry
     * @throws ConnectModuleValidationException if the syntax or semantics of the module list entry is invalid
     */
    @Override
    public List<T> deserializeAddonDescriptorModules(String jsonModuleListEntry, ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException
    {
        JsonElement modulesElement = new JsonParser().parse(jsonModuleListEntry);
        Gson gson = ConnectModulesGsonFactory.getGson();
        List<T> beans;
        if (modulesElement.isJsonObject())
        {
            assertMultipleModulesNotAllowed(descriptor);
            T module = gson.fromJson(jsonModuleListEntry, getMeta().getBeanClass());
            beans = Collections.singletonList(module);
        }
        else
        {
            assertMultipleModulesAllowed(descriptor);
            beans = new ArrayList<>();
            for (JsonElement moduleElement : modulesElement.getAsJsonArray())
            {
                beans.add(gson.fromJson(moduleElement, getMeta().getBeanClass()));
            }
        }
        return beans;
    }

    /**
     * Validates the given JSON module list entry against the given JSON schema and asserts that the result is valid.
     *
     * @param jsonModuleListEntry the string representation of the module list entry JSON element
     * @param descriptor the add-on descriptor (without the module list)
     * @param schemaUrl the URL of the JSON schema resource
     * @param schemaValidator the JSON schema validator to use
     * @throws IllegalStateException if a valid JSON schema cannot be read from the provided URL
     * @throws ConnectModuleSchemaValidationException if the module list entry was not well-formed or did not match the schema
     */
    protected void assertDescriptorValidatesAgainstSchema(String jsonModuleListEntry,
            ShallowConnectAddonBean descriptor,
            URL schemaUrl,
            ConnectJsonSchemaValidator schemaValidator)
            throws ConnectModuleSchemaValidationException
    {
        String modules = String.format("{\"%s\": %s}", getMeta().getDescriptorKey(), jsonModuleListEntry);
        try
        {
            schemaValidator.assertValidDescriptor(modules, schemaUrl);
        } catch (ConnectJsonSchemaValidationException e)
        {
            throw new ConnectModuleSchemaValidationException(descriptor, getMeta(), e);
        }
    }

    /**
     * Asserts that the module type accepts multiple modules.
     *
     * @param descriptor the add-on descriptor (without the module list)
     * @throws ConnectModuleValidationException if the assertion fails
     */
    private void assertMultipleModulesAllowed(ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException
    {
        if (!getMeta().multipleModulesAllowed())
        {
            throw new ConnectModuleValidationException(descriptor, getMeta(), "Modules should be provided as a JSON array of objects.", null, null);
        }
    }

    /**
     * Asserts that the module type only accepts a single module.
     *
     * @param descriptor the add-on descriptor (without the module list)
     * @throws ConnectModuleValidationException if the assertion fails
     */
    private void assertMultipleModulesNotAllowed(ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException
    {
        if (getMeta().multipleModulesAllowed())
        {
            throw new ConnectModuleValidationException(descriptor, getMeta(), "A single module should be provided as a JSON object.", null, null);
        }
    }
}
