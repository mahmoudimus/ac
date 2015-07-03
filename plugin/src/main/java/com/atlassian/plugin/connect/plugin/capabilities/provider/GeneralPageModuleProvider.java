package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.capabilities.provider.AbstractConnectPageModuleProvider;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GeneralPageModuleProvider extends AbstractConnectPageModuleProvider
{
    public static final String DESCRIPTOR_KEY = "generalPages";
    public static final String ATL_GENERAL_DECORATOR = "atl.general";
    private final ProductAccessor productAccessor;

    @Autowired
    public GeneralPageModuleProvider(IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
                                     IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                     WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                     ProductAccessor productAccessor)
    {
        super(iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry, webItemModuleDescriptorFactory);
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
    
    @Override
    public String getDescriptorKey()
    {
        return DESCRIPTOR_KEY;
    }
}
