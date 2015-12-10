package com.atlassian.plugin.connect.spi.web.item;

import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;

public interface ProductSpecificWebSectionModuleDescriptorFactory
{
    WebSectionModuleDescriptor createWebSectionModuleDescriptor();
}
