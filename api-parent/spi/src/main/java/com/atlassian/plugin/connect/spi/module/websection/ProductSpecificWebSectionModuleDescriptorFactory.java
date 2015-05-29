package com.atlassian.plugin.connect.spi.module.websection;

import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;

public interface ProductSpecificWebSectionModuleDescriptorFactory
{
    WebSectionModuleDescriptor createWebSectionModuleDescriptor();
}
