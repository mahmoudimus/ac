package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.descriptor.IFramePageServletDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.sal.api.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AdminPageModuleProvider extends AbstractAdminPageModuleProvider
{

    @Autowired
    public AdminPageModuleProvider(WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                   IFramePageServletDescriptorFactory servletDescriptorFactory,
                                   ProductAccessor productAccessor,
                                   UserManager userManager)
    {
        super(webItemModuleDescriptorFactory, servletDescriptorFactory, productAccessor, userManager, null);
    }
}
