package com.atlassian.plugin.connect.stash.capabilities.descriptor;

import com.atlassian.plugin.connect.spi.module.websection.ProductSpecificWebSectionModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.StashComponent;
import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;

@StashComponent
public class StashWebSectionModuleDescriptorFactory implements ProductSpecificWebSectionModuleDescriptorFactory
{
    @Override
    public WebSectionModuleDescriptor createWebSectionModuleDescriptor()
    {
        return null;
    }
}
