package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.spi.module.UserIsAdminCondition;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;

public abstract class AbstractAdminPageModuleProvider extends AbstractConnectPageModuleProvider
{
    private static final String ADMIN_PAGE_DECORATOR = "atl.admin";

    public AbstractAdminPageModuleProvider(IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
            ProductAccessor productAccessor,
            UserManager userManager, @Nullable String sectionKey)
    {
        super(iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry, webItemModuleDescriptorFactory,
                ADMIN_PAGE_DECORATOR, sectionKey != null ? sectionKey : productAccessor.getPreferredAdminSectionKey(),
                productAccessor.getPreferredAdminWeight(), "",
                ImmutableMap.<String, String>of(), new UserIsAdminCondition(userManager), null);
    }
}
