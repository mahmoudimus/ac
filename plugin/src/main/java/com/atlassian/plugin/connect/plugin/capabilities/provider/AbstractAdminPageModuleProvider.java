package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.spi.condition.UserIsAdminCondition;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.web.Condition;
import com.atlassian.sal.api.ApplicationProperties;

import java.util.Collections;

public abstract class AbstractAdminPageModuleProvider extends AbstractConnectPageModuleProvider
{
    private final ProductAccessor productAccessor;

    public AbstractAdminPageModuleProvider(IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
            ProductAccessor productAccessor,
            ApplicationProperties applicationProperties)
    {
        super(iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry, webItemModuleDescriptorFactory, applicationProperties);
        this.productAccessor = productAccessor;
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
    protected Iterable<Class<? extends Condition>> getConditionClasses()
    {
        return Collections.<Class<? extends Condition>>singletonList(UserIsAdminCondition.class);
    }

    @Override
    protected boolean needsEscaping()
    {
        return productAccessor.needsAdminPageNameEscaping();
    }
}
