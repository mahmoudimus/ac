package com.atlassian.labs.remoteapps.descriptor.factory;

import com.atlassian.labs.remoteapps.installer.AccessLevel;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import org.osgi.framework.BundleContext;

/**
 *
 */
public interface UserAccessLevelDescriptorFactory
{
    public ModuleDescriptor createWebItemModuleDescriptor(BundleContext targetBundleContext);
}
