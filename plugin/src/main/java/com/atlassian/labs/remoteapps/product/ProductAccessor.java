package com.atlassian.labs.remoteapps.product;

import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;

import java.security.Principal;

/**
 *
 */
public interface ProductAccessor
{
    WebItemModuleDescriptor createWebItemModuleDescriptor();

    String getPreferredAdminSectionKey();
    int getPreferredAdminWeight();

    String getKey();
}
