package com.atlassian.plugin.connect.core.util;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 */
public class OsgiServiceUtils
{

    public static <T> T getService(BundleContext bundleContext, Class<T> interfaceClass)
    {
        if (bundleContext != null)
        {
            ServiceReference ref = bundleContext.getServiceReference(interfaceClass.getName());
            if (ref == null)
            {
                throw new IllegalArgumentException("Cannot find service " + interfaceClass.getName());
            }
            return interfaceClass.cast(bundleContext.getService(ref));
        }
        else
        {
            throw new IllegalStateException("Cannot retrieve service: " + interfaceClass);
        }
    }
}
