package com.atlassian.plugin.connect.plugin.web.page;

import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.web.condition.ConditionClassAccessor;
import com.atlassian.plugin.connect.api.web.condition.ConditionLoadingValidator;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.GeneralPageModuleMeta;
import com.atlassian.plugin.connect.spi.ProductAccessor;
import com.atlassian.plugin.connect.spi.lifecycle.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GeneralPageModuleProvider extends AbstractGeneralPageModuleProvider
{

    private static final GeneralPageModuleMeta META = new GeneralPageModuleMeta();

    @Autowired
    public GeneralPageModuleProvider(PluginRetrievalService pluginRetrievalService,
            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
            ConditionClassAccessor conditionClassAccessor,
            ConditionLoadingValidator conditionLoadingValidator,
            ProductAccessor productAccessor,
            ConnectJsonSchemaValidator schemaValidator)
    {
        super(pluginRetrievalService, iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry,
                webItemModuleDescriptorFactory, conditionClassAccessor, schemaValidator, conditionLoadingValidator, productAccessor);
    }

    @Override
    public ConnectModuleMeta<ConnectPageModuleBean> getMeta()
    {
        return META;
    }
}
