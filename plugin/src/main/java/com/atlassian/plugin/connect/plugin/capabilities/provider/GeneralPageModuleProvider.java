package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.sal.api.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GeneralPageModuleProvider extends AbstractConnectPageModuleProvider
{
    public static final String ATL_GENERAL_DECORATOR = "atl.general";
    private final ProductAccessor productAccessor;

    @Autowired
    public GeneralPageModuleProvider(IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
            ProductAccessor productAccessor,
            ApplicationProperties applicationProperties)
    {
        super(iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry, webItemModuleDescriptorFactory, applicationProperties);
        this.productAccessor = productAccessor;
    }

    @Override
    protected int getDefaultWeight()
    {
        return productAccessor.getPreferredGeneralWeight();
    }

    @Override
    protected String getDefaultSection()
    {
        return productAccessor.getPreferredGeneralSectionKey();
    }

    @Override
    protected String getDecorator()
    {
        return ATL_GENERAL_DECORATOR;
    }

}
