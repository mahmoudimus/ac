package com.atlassian.plugin.connect.plugin.web.page;

import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.lifecycle.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.api.web.condition.ConditionClassAccessor;
import com.atlassian.plugin.connect.api.web.condition.ConditionLoadingValidator;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.spi.lifecycle.AbstractConnectPageModuleProvider;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;

import java.net.URL;
import java.util.List;

public abstract class AbstractConnectCorePageModuleProvider extends AbstractConnectPageModuleProvider {

    private static final String SCHEMA_PATH = "/schema/common-schema.json";

    protected final PluginRetrievalService pluginRetrievalService;
    private final ConnectJsonSchemaValidator schemaValidator;

    public AbstractConnectCorePageModuleProvider(PluginRetrievalService pluginRetrievalService,
                                                 IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
                                                 IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                                 WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                                 ConditionClassAccessor conditionClassAccessor,
                                                 ConnectJsonSchemaValidator schemaValidator,
                                                 ConditionLoadingValidator conditionLoadingValidator) {
        super(pluginRetrievalService, iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry,
                webItemModuleDescriptorFactory, conditionClassAccessor, conditionLoadingValidator);
        this.pluginRetrievalService = pluginRetrievalService;
        this.schemaValidator = schemaValidator;
    }

    @Override
    public List<ConnectPageModuleBean> deserializeAddonDescriptorModules(String jsonModuleListEntry,
                                                                         ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException {
        URL schemaUrl = pluginRetrievalService.getPlugin().getResource(SCHEMA_PATH);
        assertDescriptorValidatesAgainstSchema(jsonModuleListEntry, descriptor, schemaUrl, schemaValidator);
        return super.deserializeAddonDescriptorModules(jsonModuleListEntry, descriptor);
    }
}
