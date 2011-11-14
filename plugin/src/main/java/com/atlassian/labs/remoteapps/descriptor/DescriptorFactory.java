package com.atlassian.labs.remoteapps.descriptor;

import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import org.osgi.framework.BundleContext;

/**
 *
 */
public interface DescriptorFactory
{
    public ModuleDescriptor createWebItemModuleDescriptor(BundleContext targetBundleContext);
}
