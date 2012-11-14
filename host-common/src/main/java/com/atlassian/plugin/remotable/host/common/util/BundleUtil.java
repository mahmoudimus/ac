package com.atlassian.plugin.remotable.host.common.util;

import com.atlassian.plugin.osgi.factory.OsgiPlugin;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import static com.google.common.collect.Lists.*;

public final class BundleUtil
{
    private static final Bundle NOT_FOUND_BUNDLE = null;

    private BundleUtil()
    {
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
