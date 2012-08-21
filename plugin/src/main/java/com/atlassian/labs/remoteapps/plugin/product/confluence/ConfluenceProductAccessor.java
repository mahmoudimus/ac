package com.atlassian.labs.remoteapps.plugin.product.confluence;

import com.atlassian.confluence.plugin.descriptor.web.descriptors.ConfluenceWebItemModuleDescriptor;
import com.atlassian.labs.remoteapps.plugin.product.ProductAccessor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 *
 */
public class ConfluenceProductAccessor implements ProductAccessor
{

    @Override
    public WebItemModuleDescriptor createWebItemModuleDescriptor()
    {
        return new ConfluenceWebItemModuleDescriptor();
    }

    @Override
    public String getPreferredAdminSectionKey()
    {
        return "system.admin/admin.pages";
    }

    @Override
    public int getPreferredAdminWeight()
    {
        return 100;
    }

    @Override
    public String getKey()
    {
        return "confluence";
    }

    @Override
    public int getPreferredGeneralWeight()
    {
        return 1000;
    }

    @Override
    public String getPreferredGeneralSectionKey()
    {
        return "system.browse";
    }

    @Override
    public int getPreferredProfileWeight()
    {
        return 100;
    }

    @Override
    public String getPreferredProfileSectionKey()
    {
        return "system.profile"; 
    }

    @Override
    public Map<String, String> getLinkContextParams()
    {
        return ImmutableMap.of(
                "page_id", "$!helper.page.id",
                "page_type", "$!helper.page.type");
    }
}
