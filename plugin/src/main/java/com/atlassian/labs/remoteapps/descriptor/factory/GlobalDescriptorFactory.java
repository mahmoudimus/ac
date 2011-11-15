package com.atlassian.labs.remoteapps.descriptor.factory;

import com.atlassian.labs.remoteapps.installer.AccessLevel;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.plugin.ModuleDescriptor;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class GlobalDescriptorFactory
{
    private final ProductAccessor productAccessor;

    @Autowired
    public GlobalDescriptorFactory(ProductAccessor productAccessor)
    {
        this.productAccessor = productAccessor;
    }

    public ModuleDescriptor createWebItemModuleDescriptor()
    {
        return productAccessor.createWebItemModuleDescriptor();
    }
}
