package com.atlassian.labs.remoteapps.util;

import com.atlassian.plugin.osgi.factory.OsgiPlugin;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;

/**
 *
 */
public class BundleUtil
{
    private static final Logger log = LoggerFactory.getLogger(BundleUtil.class);
    public static Bundle findBundleForPlugin(BundleContext bundleContext, String pluginKey)
    {
        for (Bundle bundle : bundleContext.getBundles())
        {
            String maybePluginKey = (String) bundle.getHeaders().get(OsgiPlugin.ATLASSIAN_PLUGIN_KEY);
            if (pluginKey.equals(maybePluginKey))
            {
                return bundle;
            }
        }
        return null;
    }
}
