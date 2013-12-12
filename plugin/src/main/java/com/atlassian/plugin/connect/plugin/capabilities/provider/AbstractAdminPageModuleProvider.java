package com.atlassian.plugin.connect.plugin.capabilities.provider;

import javax.annotation.Nullable;

import com.atlassian.plugin.connect.plugin.capabilities.descriptor.IFramePageServletDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.UserIsAdminCondition;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableMap;

public abstract class AbstractAdminPageModuleProvider extends AbstractConnectPageModuleProvider
{
    private static final String ADMIN_PAGE_DECORATOR = "atl.admin";

    public AbstractAdminPageModuleProvider(WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                           IFramePageServletDescriptorFactory servletDescriptorFactory,
                                           ProductAccessor productAccessor,
                                           UserManager userManager, @Nullable String sectionKey)
    {
        super(webItemModuleDescriptorFactory, servletDescriptorFactory, ADMIN_PAGE_DECORATOR,
                sectionKey != null ? sectionKey : productAccessor.getPreferredAdminSectionKey(),
                productAccessor.getPreferredAdminWeight(), "",
                ImmutableMap.<String, String>of(), new UserIsAdminCondition(userManager), null);
    }
}
