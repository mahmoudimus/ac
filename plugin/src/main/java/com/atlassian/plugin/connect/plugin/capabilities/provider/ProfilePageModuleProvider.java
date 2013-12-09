package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.descriptor.IFramePageServletDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@ConfluenceComponent
public class ProfilePageModuleProvider extends AbstractConnectPageModuleProvider
{
    public static final String PROFILE_PAGE_DECORATOR = "atl.userprofile";

    @Autowired
    public ProfilePageModuleProvider(WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                     IFramePageServletDescriptorFactory servletDescriptorFactory,
                                     ProductAccessor productAccessor)
    {
        super(webItemModuleDescriptorFactory, servletDescriptorFactory, PROFILE_PAGE_DECORATOR,
                // Note the old code never specified these so would have defaulted to the general page values. Seems odd but preserving
//                productAccessor.getPreferredProfileSectionKey(), productAccessor.getPreferredProfileWeight(),
                productAccessor.getPreferredGeneralSectionKey(), productAccessor.getPreferredGeneralWeight(),
                "", ImmutableMap.<String, String>of(), new AlwaysDisplayCondition(), null);
    }
}
