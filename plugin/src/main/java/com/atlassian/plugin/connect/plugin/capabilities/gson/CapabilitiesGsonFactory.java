package com.atlassian.plugin.connect.plugin.capabilities.gson;

import java.lang.reflect.Type;
import java.util.List;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConditionalBean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.osgi.framework.BundleContext;

/**
 * @since 1.0
 */
public class CapabilitiesGsonFactory
{
    public static GsonBuilder getGsonBuilder()
    {
        return getGsonBuilder(null);
    }

    public static GsonBuilder getGsonBuilder(BundleContext bundleContext)
    {
        Type conditionalType = new TypeToken<List<ConditionalBean>>(){}.getType();
        
        return new GsonBuilder()
                .registerTypeAdapterFactory(new CapabilityMapAdapterFactory(bundleContext))
                //.registerTypeAdapterFactory(new ConditionalTypeAdapterFactory())
                .registerTypeAdapter(conditionalType,new ConditionalBeanSerializer())
                ;
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
