package com.atlassian.plugin.connect.plugin.module.webitem;

import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;

/**
 * Implementations should create product-specific WebItem module descriptors, e.g. JiraWebItemModuleDescriptor.
 */
public interface ProductSpecificWebItemModuleDescriptorFactory
{
    WebItemModuleDescriptor createWebItemModuleDescriptor(
            String url
            , String pluginKey
            , String moduleKey
            , boolean absolute
            , AddOnUrlContext addOnUrlContext
            , boolean isDialog
            , String section);
}
