package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.sal.api.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigurePageModuleProvider extends AbstractAdminPageModuleProvider
{
    private static final String THE_SECTION_U_HAVE_WHEN_UR_NOT_HAVING_A_SECTION = "no-section";

    // TODO: The old version used to pass in productAccessor.getLinkContextParams() to the webitem builder
    // but as far as I can tell this would have no effect as the remote page builder will overwrite it always.
    // Does that mean it was not needed or is this an existing bug?

    @Autowired
    public ConfigurePageModuleProvider(IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
            ProductAccessor productAccessor, UserManager userManager)
    {
        super(iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry, webItemModuleDescriptorFactory,
                productAccessor, userManager, THE_SECTION_U_HAVE_WHEN_UR_NOT_HAVING_A_SECTION);
    }

}
