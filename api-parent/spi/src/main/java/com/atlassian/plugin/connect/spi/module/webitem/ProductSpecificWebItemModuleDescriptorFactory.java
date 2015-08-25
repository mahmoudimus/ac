package com.atlassian.plugin.connect.spi.module.webitem;

import com.atlassian.plugin.connect.api.module.webitem.WebItemModuleDescriptorData;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;

/**
 * Implementations should create product-specific WebItem module descriptors, e.g. JiraWebItemModuleDescriptor.
 */
public interface ProductSpecificWebItemModuleDescriptorFactory
{
    WebItemModuleDescriptor createWebItemModuleDescriptor(WebItemModuleDescriptorData webItemModuleDescriptorData);
}
