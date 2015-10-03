package com.atlassian.plugin.connect.confluence.capabilities.provider;

import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.ProfilePageModuleMeta;
import com.atlassian.plugin.connect.spi.capabilities.provider.PageConditionsValidator;
import com.atlassian.plugin.connect.spi.capabilities.provider.AbstractConnectPageModuleProvider;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import org.springframework.beans.factory.annotation.Autowired;

@ConfluenceComponent
public class ProfilePageModuleProvider extends AbstractConnectPageModuleProvider
{
    private final ProductAccessor productAccessor;

    @Autowired
    public ProfilePageModuleProvider(IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
                                     IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                     WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                     PageConditionsValidator pageConditionsValidator,
                                     ProductAccessor productAccessor)
    {
        super(iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry, webItemModuleDescriptorFactory, pageConditionsValidator);
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
    public String getSchemaPrefix()
    {
        return "confluence";
    }

    @Override
    public ConnectModuleMeta<ConnectPageModuleBean> getMeta()
    {
        return new ProfilePageModuleMeta();
    }
}
