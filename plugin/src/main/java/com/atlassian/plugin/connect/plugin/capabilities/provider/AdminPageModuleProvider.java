package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.descriptor.IFramePageServletDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.UserIsAdminCondition;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AdminPageModuleProvider extends AbstractConnectPageModuleProvider
{
    private static final String ADMIN_PAGE_DECORATOR = "atl.admin";

    @Autowired
    public AdminPageModuleProvider(WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
            IFramePageServletDescriptorFactory servletDescriptorFactory, ProductAccessor productAccessor,
            UserManager userManager)
    {
        super(webItemModuleDescriptorFactory, servletDescriptorFactory, ADMIN_PAGE_DECORATOR,
                productAccessor.getPreferredAdminSectionKey(), productAccessor.getPreferredAdminWeight(), "",
                ImmutableMap.<String, String>of(), new UserIsAdminCondition(userManager), null);
    }
}
