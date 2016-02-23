package com.atlassian.plugin.connect.jira;

import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.spi.lifecycle.AbstractConnectModuleProvider;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;

import java.net.URL;
import java.util.List;

public abstract class AbstractJiraConnectModuleProvider<T extends BaseModuleBean> extends AbstractConnectModuleProvider<T> {

    private static final String SCHEMA_PATH = "/schema/jira-schema.json";

    protected final PluginRetrievalService pluginRetrievalService;
    private final ConnectJsonSchemaValidator schemaValidator;

    public AbstractJiraConnectModuleProvider(PluginRetrievalService pluginRetrievalService, ConnectJsonSchemaValidator schemaValidator) {
        this.pluginRetrievalService = pluginRetrievalService;
        this.schemaValidator = schemaValidator;
    }

    @Override
    public List<T> deserializeAddonDescriptorModules(String jsonModuleListEntry, ShallowConnectAddonBean descriptor)
            throws ConnectModuleValidationException {
        URL schemaUrl = pluginRetrievalService.getPlugin().getResource(SCHEMA_PATH);
        assertDescriptorValidatesAgainstSchema(jsonModuleListEntry, descriptor, schemaUrl, schemaValidator);
        return super.deserializeAddonDescriptorModules(jsonModuleListEntry, descriptor);
    }
}
