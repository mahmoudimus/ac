package com.atlassian.labs.remoteapps.product.confluence;

import com.atlassian.confluence.plugin.descriptor.web.descriptors.ConfluenceWebItemModuleDescriptor;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.labs.remoteapps.product.WebSudoElevator;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;

import java.util.Map;

import static java.util.Collections.singletonMap;

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
        return singletonMap("page_id", "$!helper.page.id");
    }
}
