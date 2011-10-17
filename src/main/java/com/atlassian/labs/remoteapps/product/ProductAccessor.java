package com.atlassian.labs.remoteapps.product;

import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;

/**
 *
 */
public interface ProductAccessor
{
    WebItemModuleDescriptor createWebItemModuleDescriptor();

    String getPreferredAdminSectionKey();
    int getPreferredAdminWeight();

}
