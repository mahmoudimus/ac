package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.descriptor.IFramePageServletDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.plugin.capabilities.provider.AbstractConnectPageModuleProvider.ConnectPageIFrameParams.withGeneralPage;

@Component
public class GeneralPageModuleProvider extends AbstractConnectPageModuleProvider
{
    private static final String GENERAL_PAGE_DECORATOR = "atl.general";

    @Autowired
    public GeneralPageModuleProvider(WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                     IFramePageServletDescriptorFactory servletDescriptorFactory,
                                     ProductAccessor productAccessor)
    {
        super(webItemModuleDescriptorFactory, servletDescriptorFactory, productAccessor,
                GENERAL_PAGE_DECORATOR, "", ImmutableMap.<String, String>of(), new AlwaysDisplayCondition(),
                withGeneralPage());
    }
}
