package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.sal.api.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;

@ConfluenceComponent
public class ProfilePageModuleProvider extends AbstractConnectPageModuleProvider
{
    private final ProductAccessor productAccessor;

    @Autowired
    public ProfilePageModuleProvider(IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
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
        return productAccessor.getPreferredProfileWeight();
    }

    @Override
    protected String getDefaultSection()
    {
        return productAccessor.getPreferredProfileSectionKey();
    }

    @Override
    protected String getDecorator()
    {
        return "atl.userprofile";
    }

}
