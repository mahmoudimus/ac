package com.atlassian.plugin.connect.plugin.capabilities.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.osgi.framework.BundleContext;

/**
 * @since version
 */
public class CapabilitiesGsonFactory
{
    public static GsonBuilder getGsonBuilder()
    {
        return new GsonBuilder().registerTypeAdapterFactory(new CapabilityMapAdapterFactory(null));
    }

    public static GsonBuilder getGsonBuilder(BundleContext bundleContext)
    {
        return new GsonBuilder().registerTypeAdapterFactory(new CapabilityMapAdapterFactory(bundleContext));
    }
    
    public static Gson getGson()
    {
        return getGsonBuilder().create();
    }

    public static Gson getGson(BundleContext bundleContext)
    {
        return getGsonBuilder(bundleContext).create();
    }
}
