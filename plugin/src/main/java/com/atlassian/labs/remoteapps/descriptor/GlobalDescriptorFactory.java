package com.atlassian.labs.remoteapps.descriptor;

import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.plugin.ModuleDescriptor;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
public class GlobalDescriptorFactory implements DescriptorFactory
{
    private final ProductAccessor productAccessor;

    @Autowired
    public GlobalDescriptorFactory(ProductAccessor productAccessor)
    {
        this.productAccessor = productAccessor;
    }

    @Override
    public ModuleDescriptor createWebItemModuleDescriptor(BundleContext targetBundleContext)
    {
        return productAccessor.createWebItemModuleDescriptor();
    }
}
