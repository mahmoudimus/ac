package com.atlassian.plugin.connect.confluence.capabilities.descriptor;

import com.atlassian.confluence.plugin.descriptor.web.descriptors.ConfluenceWebSectionModuleDescriptor;
import com.atlassian.plugin.connect.spi.module.websection.ProductSpecificWebSectionModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;

@ConfluenceComponent
public class ConfluenceWebSectionModuleDescriptorFactory implements ProductSpecificWebSectionModuleDescriptorFactory
{
    public ConfluenceWebSectionModuleDescriptorFactory()
    {
    }

    @Override
    public WebSectionModuleDescriptor createWebSectionModuleDescriptor()
    {
        return new ConfluenceWebSectionModuleDescriptor();
    }
}
