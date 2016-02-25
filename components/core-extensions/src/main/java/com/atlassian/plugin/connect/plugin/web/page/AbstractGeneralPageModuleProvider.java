package com.atlassian.plugin.connect.plugin.web.page;

import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.lifecycle.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.api.web.condition.ConditionClassAccessor;
import com.atlassian.plugin.connect.api.web.condition.ConditionLoadingValidator;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.spi.ProductAccessor;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;

public abstract class AbstractGeneralPageModuleProvider extends AbstractConnectCorePageModuleProvider {

    private static final String ATL_GENERAL_DECORATOR = "atl.general";

    protected final ProductAccessor productAccessor;

    public AbstractGeneralPageModuleProvider(PluginRetrievalService pluginRetrievalService,
                                             IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
                                             IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                             WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                             ConditionClassAccessor conditionClassAccessor,
                                             ConnectJsonSchemaValidator schemaValidator,
                                             ConditionLoadingValidator conditionLoadingValidator,
                                             ProductAccessor productAccessor) {
        super(pluginRetrievalService, iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry,
                webItemModuleDescriptorFactory, conditionClassAccessor, schemaValidator, conditionLoadingValidator);
        this.productAccessor = productAccessor;
    }

    @Override
    protected String getDecorator() {
        return ATL_GENERAL_DECORATOR;
    }

    @Override
    protected String getDefaultSection() {
        return productAccessor.getPreferredGeneralSectionKey();
    }

    @Override
    protected int getDefaultWeight() {
        return productAccessor.getPreferredGeneralWeight();
    }
}
