package com.atlassian.plugin.connect.plugin.util;

import com.atlassian.plugin.osgi.factory.OsgiPlugin;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import static com.google.common.collect.Lists.newArrayList;

public final class BundleUtil
{
    private static final Bundle NOT_FOUND_BUNDLE = null;

    private BundleUtil()
    {
    }

    /**
     * Returns the version number from the context bundle.
     * Can be used to get the version of the Connect p2 plugin.
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

    public static Bundle findBundleForPlugin(BundleContext bundleContext, String pluginKey)
    {
        return findBundleForPlugin(newArrayList(bundleContext.getBundles()), pluginKey);
    }

    private static Bundle findBundleForPlugin(Iterable<Bundle> bundles, final String pluginKey)
    {
        return Iterables.find(bundles,
                new Predicate<Bundle>()
                {
                    @Override
                    public boolean apply(Bundle b)
                    {
                        return pluginKey.equals(b.getHeaders().get(OsgiPlugin.ATLASSIAN_PLUGIN_KEY));
                    }
                },
                NOT_FOUND_BUNDLE);
    }

    public static Bundle findBundleWithName(final BundleContext bundleContext, final String symbolicName)
    {
        return findBundleWithName(newArrayList(bundleContext.getBundles()), symbolicName);
    }

    private static Bundle findBundleWithName(Iterable<Bundle> bundles, final String symbolicName)
    {
        return Iterables.find(bundles,
                new Predicate<Bundle>()
                {
                    @Override
                    public boolean apply(Bundle b)
                    {
                        return b.getSymbolicName().equals(symbolicName);
                    }
                },
                NOT_FOUND_BUNDLE);
    }

    public static Iterable<String> toBundleNames(Bundle[] bundles)
    {
        return Iterables.transform(newArrayList(bundles), new Function<Bundle, String>()
        {
            @Override
            public String apply(Bundle b)
            {
                return b.getSymbolicName();
            }
        });
    }
}
