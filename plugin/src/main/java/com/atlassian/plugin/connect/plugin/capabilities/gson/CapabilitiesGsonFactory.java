package com.atlassian.plugin.connect.plugin.capabilities.gson;

import com.atlassian.plugin.connect.api.scopes.ScopeName;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConditionalBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.LifecycleBean;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.osgi.framework.BundleContext;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

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
        Type capabilityList = new TypeToken<List>(){}.getType();
        Type mapStringType = new TypeToken<Map<String,String>>(){}.getType();
        return new GsonBuilder()
                .registerTypeAdapter(conditionalType,new ConditionalBeanSerializer())
                .registerTypeAdapter(LifecycleBean.class, new LifecycleSerializer())
                .registerTypeHierarchyAdapter(List.class, new IgnoredEmptyCollectionSerializer())
                .registerTypeAdapter(mapStringType, new IgnoredEmptyMapSerializer())
                .registerTypeAdapter(String.class,new EmptyStringIgnoringTypeAdapter().nullSafe())
                .registerTypeAdapterFactory(new LowercaseEnumTypeAdapterFactory())
                .registerTypeAdapter(ScopeName.class, new ScopeNameSerializer())
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
