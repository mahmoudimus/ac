package com.atlassian.plugin.connect.confluence.web;

import com.atlassian.confluence.plugin.descriptor.web.descriptors.ConfluenceWebSectionModuleDescriptor;
import com.atlassian.plugin.connect.spi.web.item.ProductSpecificWebSectionModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;

@ConfluenceComponent
public class ConfluenceWebSectionModuleDescriptorFactory implements ProductSpecificWebSectionModuleDescriptorFactory {
    public ConfluenceWebSectionModuleDescriptorFactory() {
    }

    @Override
    public WebSectionModuleDescriptor createWebSectionModuleDescriptor() {
        return new ConfluenceWebSectionModuleDescriptor();
    }
}
