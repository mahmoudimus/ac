package com.atlassian.plugin.connect.plugin.module.webitem;

import com.atlassian.plugin.connect.plugin.capabilities.beans.AddOnUrlContext;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;

/**
 * Implementations should create product-specific WebItem module descriptors, e.g. JiraWebItemModuleDescriptor.
 */
public interface ProductSpecificWebItemModuleDescriptorFactory
{
    WebItemModuleDescriptor createWebItemModuleDescriptor(String url, String linkId, boolean absolute,
                                                          AddOnUrlContext addOnUrlContext);
}
