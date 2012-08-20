package com.atlassian.labs.remoteapps.plugin.product;

import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;

import java.util.Map;

/**
 * Product-specific accessors
 */
public interface ProductAccessor
{
    WebItemModuleDescriptor createWebItemModuleDescriptor();

    String getPreferredAdminSectionKey();

    int getPreferredAdminWeight();

    String getKey();

    int getPreferredGeneralWeight();

    String getPreferredGeneralSectionKey();

    int getPreferredProfileWeight();

    String getPreferredProfileSectionKey();

    Map<String,String> getLinkContextParams();
}
