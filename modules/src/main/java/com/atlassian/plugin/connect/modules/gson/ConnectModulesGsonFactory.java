package com.atlassian.plugin.connect.modules.gson;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.google.common.base.Supplier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * @since 1.0
 */
public class ConnectModulesGsonFactory
{
    private static Type moduleJsonType = new TypeToken<Map<String, Supplier<List<ModuleBean>>>>() {}.getType();

    public static Type getModuleJsonType()
    {
        return moduleJsonType;
    }

    public static GsonBuilder getGsonBuilder()
    {
        Type conditionalListType = new TypeToken<List<ConditionalBean>>() {}.getType();
        Type mapStringType = new TypeToken<Map<String, String>>() {}.getType();

        return new GsonBuilder()
                .registerTypeAdapterFactory(new ConditionalBeanTypeAdapterFactory(conditionalListType))
                .registerTypeAdapter(LifecycleBean.class, new LifecycleSerializer())
                .registerTypeHierarchyAdapter(List.class, new IgnoredEmptyCollectionSerializer())
                .registerTypeAdapter(mapStringType, new IgnoredEmptyMapSerializer())
                .registerTypeAdapter(String.class, new EmptyStringIgnoringTypeAdapter().nullSafe())
                .registerTypeAdapterFactory(new LowercaseEnumTypeAdapterFactory())
                .registerTypeAdapterFactory(new NullIgnoringSetTypeAdapterFactory())
                .registerTypeAdapter(WebItemTargetBean.class, new WebItemTargetBeanSerializer())
                .setPrettyPrinting()
                .disableHtmlEscaping()
                ;
    }

    public static Gson getGson()
    {
        return getGsonBuilder().create();
    }

    public static ConnectAddonBean addonFromJsonWithI18nCollector(String json, Map<String,String> i18nCollector, JsonDeserializer<Map<String, Supplier<List<ModuleBean>>>> moduleDeserializer)
    {
        GsonBuilder builder = getGsonBuilder().registerTypeAdapter(moduleJsonType, moduleDeserializer);
        if(null != i18nCollector)
        {
            builder = builder.registerTypeAdapter(I18nProperty.class, new I18nCollectingDeserializer(i18nCollector));
        }
        return builder.create().fromJson(json, ConnectAddonBean.class);
    }

    public static String addonBeanToJson(ConnectAddonBean bean)
    {
        Gson gson = getGsonBuilder().registerTypeAdapter(ConnectModulesGsonFactory.getModuleJsonType(), new DefaultModuleSerializer()).create();
        return gson.toJson(bean);
    }
}
