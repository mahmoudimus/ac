package com.atlassian.plugin.connect.plugin.module.webitem;

import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;

/**
 * Implementations should create product-specific WebItem module descriptors, e.g. JiraWebItemModuleDescriptor.
 */
public interface WebItemModuleDescriptorFactory
{
    WebItemModuleDescriptor createWebItemModuleDescriptor(String url, String moduleKey, boolean absolute);
}
