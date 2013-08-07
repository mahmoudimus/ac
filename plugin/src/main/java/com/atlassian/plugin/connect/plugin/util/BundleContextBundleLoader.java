package com.atlassian.plugin.connect.plugin.util;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 *
 */
public class BundleContextBundleLoader implements BundleLocator
{
    private final BundleContext bundleContext;

    public BundleContextBundleLoader(BundleContext bundleContext)
    {
        this.bundleContext = bundleContext;
    }

    @Override
    public Bundle getBundle(String pluginKey)
    {
        return BundleUtil.findBundleForPlugin(bundleContext, pluginKey);
    }
}
