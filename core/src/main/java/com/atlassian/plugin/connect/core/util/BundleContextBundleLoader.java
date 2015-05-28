package com.atlassian.plugin.connect.core.util;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class BundleContextBundleLoader implements BundleLocator
{
    private final BundleContext bundleContext;

    @Autowired
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
