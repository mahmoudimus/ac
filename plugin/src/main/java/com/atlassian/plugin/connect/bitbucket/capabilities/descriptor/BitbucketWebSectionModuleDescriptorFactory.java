package com.atlassian.plugin.connect.bitbucket.capabilities.descriptor;

import com.atlassian.plugin.connect.spi.module.websection.ProductSpecificWebSectionModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.BitbucketComponent;
import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;

@BitbucketComponent
public class BitbucketWebSectionModuleDescriptorFactory implements ProductSpecificWebSectionModuleDescriptorFactory
{
    @Override
    public WebSectionModuleDescriptor createWebSectionModuleDescriptor()
    {
        return null;
    }
}
