package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.spi.module.UserIsAdminCondition;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.web.Condition;
import com.atlassian.sal.api.user.UserManager;

public abstract class AbstractAdminPageModuleProvider extends AbstractConnectPageModuleProvider
{
    private final ProductAccessor productAccessor;
    private final UserIsAdminCondition userIsAdminCondition;

    public AbstractAdminPageModuleProvider(IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
            ProductAccessor productAccessor, UserManager userManager)
    {
        super(iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry, webItemModuleDescriptorFactory);
        this.productAccessor = productAccessor;
        userIsAdminCondition = new UserIsAdminCondition(userManager);
    }

    @Override
    protected String getDecorator()
    {
        return "atl.admin";
    }

    @Override
    protected String getDefaultSection()
    {
        return productAccessor.getPreferredAdminSectionKey();
    }

    @Override
    protected int getDefaultWeight()
    {
        return productAccessor.getPreferredAdminWeight();
    }

    @Override
    protected Condition getCondition()
    {
        return userIsAdminCondition;
    }

}
