package com.atlassian.plugin.connect.confluence.capabilities.provider;

import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.spi.capabilities.provider.AbstractConnectPageModuleProvider;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import org.springframework.beans.factory.annotation.Autowired;

@ConfluenceComponent
public class ProfilePageModuleProvider extends AbstractConnectPageModuleProvider
{
    private final ProductAccessor productAccessor;

    public static final String DESCRIPTOR_KEY = "profilePages";

    @Autowired
    public ProfilePageModuleProvider(IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
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

    @Override
    public String getDescriptorKey()
    {
        return DESCRIPTOR_KEY;
    }

    @Override
    public String getSchemaPrefix()
    {
        return "confluence";
    }
}
