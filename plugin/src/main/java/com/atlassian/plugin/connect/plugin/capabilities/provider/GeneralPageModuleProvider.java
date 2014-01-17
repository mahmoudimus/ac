package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.plugin.capabilities.provider.AbstractConnectPageModuleProvider.ConnectPageIFrameParams.withGeneralPage;

@Component
public class GeneralPageModuleProvider extends AbstractConnectPageModuleProvider
{
    private static final String GENERAL_PAGE_DECORATOR = "atl.general";

    @Autowired
    public GeneralPageModuleProvider(IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
                                     IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                     WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                     ProductAccessor productAccessor)
    {
        super(iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry, webItemModuleDescriptorFactory,
                GENERAL_PAGE_DECORATOR, productAccessor.getPreferredGeneralSectionKey(),
                productAccessor.getPreferredGeneralWeight(), "",
                new AlwaysDisplayCondition(), withGeneralPage());
    }
}
