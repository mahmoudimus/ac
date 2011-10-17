package com.atlassian.labs.remoteapps.product;

import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.DefaultWebItemModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;

/**
 *
 */
public class RefappProductAccessor implements ProductAccessor
{
    private final WebInterfaceManager webInterfaceManager;

    public RefappProductAccessor(WebInterfaceManager webInterfaceManager)
    {
        this.webInterfaceManager = webInterfaceManager;
    }

    @Override
    public WebItemModuleDescriptor createWebItemModuleDescriptor()
    {
        return new DefaultWebItemModuleDescriptor(webInterfaceManager);
    }

    @Override
    public String getPreferredAdminSectionKey()
    {
        return "system.admin/general";
    }

    @Override
    public int getPreferredAdminWeight()
    {
        return 10;
    }
}
