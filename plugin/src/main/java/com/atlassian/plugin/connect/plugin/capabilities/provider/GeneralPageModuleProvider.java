package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.GeneralPageModuleMeta;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.capabilities.provider.AbstractConnectPageModuleProvider;
import com.atlassian.plugin.connect.spi.capabilities.provider.PageConditionsValidator;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
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
                                     PageConditionsValidator pageConditionsValidator,
                                     ProductAccessor productAccessor)
    {
        super(iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry, webItemModuleDescriptorFactory, pageConditionsValidator);
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
    public String getSchemaPrefix()
    {
        return "common";
    }

    @Override
    public ConnectModuleMeta<ConnectPageModuleBean> getMeta()
    {
        return new GeneralPageModuleMeta();
    }
}
