package com.atlassian.plugin.connect.modules.gson;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.beans.*;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.google.common.base.Supplier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonDeserializer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * @since 1.0
 */
public class ConnectModulesGsonFactory
{
    public static GsonBuilder getGsonBuilder(JsonDeserializer moduleDeserializer)
    {
        Type conditionalType = new TypeToken<List<ConditionalBean>>() {}.getType();
        Type mapStringType = new TypeToken<Map<String, String>>() {}.getType();
        Type mapJsonType = new TypeToken<Map<String, Supplier<List<ModuleBean>>>>() {}.getType();
        
        System.out.println("HEY WE'RE DEBUGGING HERE");
        return new GsonBuilder()
                .registerTypeAdapter(conditionalType, new ConditionalBeanSerializer())
                .registerTypeAdapter(LifecycleBean.class, new LifecycleSerializer())
                .registerTypeHierarchyAdapter(List.class, new IgnoredEmptyCollectionSerializer())
                .registerTypeAdapter(mapStringType, new IgnoredEmptyMapSerializer())
                .registerTypeAdapter(String.class, new EmptyStringIgnoringTypeAdapter().nullSafe())
                .registerTypeAdapterFactory(new LowercaseEnumTypeAdapterFactory())
                .registerTypeAdapterFactory(new NullIgnoringSetTypeAdapterFactory())
                .registerTypeAdapter(WebItemTargetBean.class, new WebItemTargetBeanSerializer())
                .registerTypeAdapter(JsonObject.class, new DynamicModuleSerializer())
                .registerTypeAdapter(mapJsonType, moduleDeserializer)
                .setPrettyPrinting()
                .disableHtmlEscaping()
                ;
    }

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
                .registerTypeAdapterFactory(new NullIgnoringSetTypeAdapterFactory())
                .registerTypeAdapter(WebItemTargetBean.class, new WebItemTargetBeanSerializer())
                .registerTypeAdapter(JsonObject.class, new DynamicModuleSerializer())
                .setPrettyPrinting()
                .disableHtmlEscaping()
                ;
    }

    public static Gson getGson(JsonDeserializer<Map<String, Supplier<List<ModuleBean>>>> moduleDeserializer)
    {
        return getGsonBuilder(moduleDeserializer).create();
    }

    public static Gson getGson()
    {
        return getGsonBuilder().create();
    }

    public static ConnectAddonBean addonFromJsonWithI18nCollector(String json, Map<String,String> i18nCollector, JsonDeserializer<Map<String, Supplier<List<ModuleBean>>>> moduleDeserializer)
    {
        Gson gson;
        if(null != i18nCollector)
        {
            gson = getGsonBuilder(moduleDeserializer).registerTypeAdapter(I18nProperty.class,new I18nCollectingDeserializer(i18nCollector)).create();
        }
        else
        {
            gson = getGson();
        }

        return gson.fromJson(json,ConnectAddonBean.class);
    }

    public static String addonBeanToJson(ConnectAddonBean bean)
    {
        return getGson().toJson(bean);
    }
}
