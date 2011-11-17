package com.atlassian.labs.remoteapps;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.labs.remoteapps.descriptor.external.AccessLevel;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.plugin.ModuleDescriptor;
import org.osgi.framework.BundleContext;

/**
 *
 */
public class GlobalAccessLevel implements AccessLevel
{
    private final ProductAccessor productAccessor;

    public GlobalAccessLevel(ProductAccessor productAccessor)
    {
        this.productAccessor = productAccessor;
    }

    @Override
    public String getId()
    {
        return "global";
    }

    @Override
    public boolean canAccessRemoteApp(String username, ApplicationLink link)
    {
        return true;
    }

    @Override
    public ModuleDescriptor createWebItemModuleDescriptor(BundleContext targetBundleContext)
    {
        return productAccessor.createWebItemModuleDescriptor();
    }
}
