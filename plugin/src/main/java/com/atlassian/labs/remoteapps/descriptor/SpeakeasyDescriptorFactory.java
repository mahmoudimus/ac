package com.atlassian.labs.remoteapps.descriptor;

import com.atlassian.labs.speakeasy.descriptor.external.DescriptorGeneratorManager;
import com.atlassian.labs.speakeasy.descriptor.external.webfragment.SpeakeasyWebItemModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
public class SpeakeasyDescriptorFactory implements DescriptorFactory
{
    private final WebResourceManager webResourceManager;
    private final ServiceTracker tracker;

    @Autowired
    public SpeakeasyDescriptorFactory(WebResourceManager webResourceManager, BundleContext bundleContext)
    {
        this.tracker = new ServiceTracker(bundleContext, DescriptorGeneratorManager.class.getName(), null);
        tracker.open();
        this.webResourceManager = webResourceManager;
    }

    @Override
    public ModuleDescriptor createWebItemModuleDescriptor(BundleContext targetBundleContext)
    {
        return new SpeakeasyWebItemModuleDescriptor(targetBundleContext, (DescriptorGeneratorManager) tracker.getService(), webResourceManager);
    }
}
