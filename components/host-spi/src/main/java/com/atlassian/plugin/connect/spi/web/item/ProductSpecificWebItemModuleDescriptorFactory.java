package com.atlassian.plugin.connect.spi.web.item;

import com.atlassian.plugin.connect.modules.beans.AddonUrlContext;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;

/**
 * Implementations should create product-specific WebItem module descriptors, e.g. JiraWebItemModuleDescriptor.
 */
public interface ProductSpecificWebItemModuleDescriptorFactory {
    WebItemModuleDescriptor createWebItemModuleDescriptor(
            String url
            , String pluginKey
            , String moduleKey
            , boolean absolute
            , AddonUrlContext addonUrlContext
            , boolean isDialog
            , String section);
}
