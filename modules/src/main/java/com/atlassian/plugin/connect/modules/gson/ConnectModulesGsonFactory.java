package com.atlassian.plugin.connect.modules.gson;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * @since 1.0
 */
public class ConnectModulesGsonFactory
{
    public static GsonBuilder getGsonBuilder()
    {
        Type conditionalType = new TypeToken<List<ConditionalBean>>() {}.getType();
        Type mapStringType = new TypeToken<Map<String, String>>() {}.getType();

        return new GsonBuilder()
                .registerTypeAdapter(conditionalType, new ConditionalBeanSerializer())
                .registerTypeAdapter(LifecycleBean.class, new LifecycleSerializer())
                .registerTypeHierarchyAdapter(List.class, new IgnoredEmptyCollectionSerializer())
                .registerTypeAdapter(mapStringType, new IgnoredEmptyMapSerializer())
                .registerTypeAdapter(String.class, new EmptyStringIgnoringTypeAdapter().nullSafe())
                .registerTypeAdapterFactory(new LowercaseEnumTypeAdapterFactory())
                ;
    }

    public static Gson getGson()
    {
        return getGsonBuilder().create();
    }
}
