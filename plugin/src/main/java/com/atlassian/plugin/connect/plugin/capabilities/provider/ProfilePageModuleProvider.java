package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import org.springframework.beans.factory.annotation.Autowired;

@ConfluenceComponent
public class ProfilePageModuleProvider extends AbstractConnectPageModuleProvider
{
    public static final String PROFILE_PAGE_DECORATOR = "atl.userprofile";

    @Autowired
    public ProfilePageModuleProvider(IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
                                     IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                     WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                     ProductAccessor productAccessor)
    {
        super(iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry, webItemModuleDescriptorFactory,
                PROFILE_PAGE_DECORATOR, productAccessor.getPreferredProfileSectionKey(),
                productAccessor.getPreferredProfileWeight(), "",
                new AlwaysDisplayCondition(), null);
    }
}
