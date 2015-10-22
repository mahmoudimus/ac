package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.capabilities.provider.AbstractConnectPageModuleProvider;
import com.atlassian.plugin.connect.spi.condition.PageConditionsFactory;
import com.atlassian.plugin.connect.spi.module.ConnectModuleValidationException;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;

import java.net.URL;
import java.util.List;

public abstract class AbstractConnectCorePageModuleProvider extends AbstractConnectPageModuleProvider
{

    private static final String SCHEMA_PATH = "/schema/common-schema.json";

    protected final PluginRetrievalService pluginRetrievalService;
    private final ConnectJsonSchemaValidator schemaValidator;

    public AbstractConnectCorePageModuleProvider(PluginRetrievalService pluginRetrievalService,
            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
            PageConditionsFactory pageConditionsFactory,
            ConnectJsonSchemaValidator schemaValidator)
    {
        super(pluginRetrievalService, iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry,
                webItemModuleDescriptorFactory, pageConditionsFactory);
        this.pluginRetrievalService = pluginRetrievalService;
        this.schemaValidator = schemaValidator;
    }

    @Override
    public List<ConnectPageModuleBean> deserializeAddonDescriptorModules(String jsonModuleListEntry,
            ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException
    {
        URL schemaUrl = pluginRetrievalService.getPlugin().getResource(SCHEMA_PATH);
        assertDescriptorValidatesAgainstSchema(jsonModuleListEntry, schemaUrl, schemaValidator);
        return super.deserializeAddonDescriptorModules(jsonModuleListEntry, descriptor);
    }
}
