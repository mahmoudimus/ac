package com.atlassian.plugin.connect.modules.gson;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleList;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetBean;
import com.atlassian.plugin.connect.modules.beans.XmlDescriptorCodeInvokedEventBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.reflect.TypeToken;

/**
 * @since 1.0
 */
public class ConnectModulesGsonFactory
{
    public static <M extends ModuleList> GsonBuilder getGsonBuilder(InstanceCreator<ModuleList> moduleListInstanceCreator)
    {
        Type conditionalType = new TypeToken<List<ConditionalBean>>() {}.getType();
        Type mapStringType = new TypeToken<Map<String, String>>() {}.getType();
        final Type moduleListType = new TypeToken<M>() {}.getType();

        return new GsonBuilder()
                .registerTypeAdapter(conditionalType, new ConditionalBeanSerializer())
                .registerTypeAdapter(LifecycleBean.class, new LifecycleSerializer())
                .registerTypeHierarchyAdapter(List.class, new IgnoredEmptyCollectionSerializer())
                .registerTypeAdapter(mapStringType, new IgnoredEmptyMapSerializer())
                .registerTypeAdapter(String.class, new EmptyStringIgnoringTypeAdapter().nullSafe())
                .registerTypeAdapterFactory(new LowercaseEnumTypeAdapterFactory())
                .registerTypeAdapterFactory(new NullIgnoringSetTypeAdapterFactory())
                .registerTypeAdapter(XmlDescriptorCodeInvokedEventBean.class, new XmlDescriptorCodeInvokedEventBeanSerializer())
                .registerTypeAdapter(WebItemTargetBean.class, new WebItemTargetBeanSerializer())
                .registerTypeAdapter(moduleListType, moduleListInstanceCreator)
                .disableHtmlEscaping()
                ;
    }

    public static <M extends ModuleList> Gson getGson(InstanceCreator<ModuleList> moduleListInstanceCreator)
    {
        return getGsonBuilder(moduleListInstanceCreator).create();
    }

    public static <M extends ModuleList> ConnectAddonBean<M>
        addonFromJsonWithI18nCollector(InstanceCreator<ModuleList> moduleListInstanceCreator,
                                       String json, Map<String,String> i18nCollector)
    {
        Gson gson;

        if(null != i18nCollector)
        {
            gson = getGsonBuilder(moduleListInstanceCreator).registerTypeAdapter(I18nProperty.class,new I18nCollectingDeserializer(i18nCollector)).create();
        }
        else
        {
            gson = getGson(moduleListInstanceCreator);
        }

        final Type type = new TypeToken<ConnectAddonBean<M>>() {}.getType();

        return gson.fromJson(json, type);
    }
}
