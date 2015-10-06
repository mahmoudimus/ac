package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.capabilities.provider.AbstractConnectPageModuleProvider;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;

public abstract class AbstractGeneralPageModuleProvider extends AbstractConnectPageModuleProvider
{

    public static final String ATL_GENERAL_DECORATOR = "atl.general";

    protected final ProductAccessor productAccessor;

    public AbstractGeneralPageModuleProvider(IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
                                             IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                             WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                             ProductAccessor productAccessor)
    {
        super(iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry, webItemModuleDescriptorFactory);
        this.productAccessor = productAccessor;
    }

    @Override
    protected String getDecorator()
    {
        return ATL_GENERAL_DECORATOR;
    }

    @Override
    protected String getDefaultSection()
    {
        return productAccessor.getPreferredGeneralSectionKey();
    }

    @Override
    protected int getDefaultWeight()
    {
        return productAccessor.getPreferredGeneralWeight();
    }
}
