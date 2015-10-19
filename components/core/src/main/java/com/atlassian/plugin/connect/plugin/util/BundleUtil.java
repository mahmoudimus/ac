package com.atlassian.plugin.connect.plugin.util;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

public final class BundleUtil
{

    private BundleUtil()
    {
    }

    /**
     * Returns the version number from the context bundle.
     * Can be used to get the version of the Connect p2 plugin.
     *
     * @param bundleContext the bundle context
     * @return the bundle version
     */
    public static String getBundleVersion(BundleContext bundleContext)
    {
        Object bundleVersion = bundleContext.getBundle().getHeaders().get(Constants.BUNDLE_VERSION);
        if(bundleVersion != null && bundleVersion instanceof String)
        {
            return (String) bundleVersion;
        }
        return null;
    }
}
