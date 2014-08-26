package com.atlassian.plugin.connect.modules.gson;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.JiraConfluenceModuleList;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleList;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetBean;
import com.atlassian.plugin.connect.modules.beans.XmlDescriptorCodeInvokedEventBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * @since 1.0
 */
public class ConnectModulesGsonFactory
{
    public static <M extends ModuleList> GsonBuilder getGsonBuilder(//Class<M> moduleListType,
                                                                    InstanceCreator<ModuleList> moduleListInstanceCreator)
    {
        Type conditionalType = new TypeToken<List<ConditionalBean>>() {}.getType();
        Type mapStringType = new TypeToken<Map<String, String>>() {}.getType();
        final Type moduleListType2 = new TypeToken<M>() {}.getType();

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
                .registerTypeAdapter(ModuleList.class, moduleListInstanceCreator)
//                .registerTypeAdapter(ModuleList.class, new ModuleListDeserializer<M>(moduleListType))
//                .registerTypeAdapter(moduleListType, new ModuleListDeserializer<M>(moduleListType))
//                .registerTypeAdapter(moduleListType2, new ModuleListDeserializer<M>(moduleListType))
//                .registerTypeHierarchyAdapter(ModuleList.class, new ModuleListDeserializer<M>(moduleListType))
//                .registerTypeHierarchyAdapter(moduleListType, new ModuleListDeserializer<M>(moduleListType))
//                .registerTypeAdapter(ConnectAddonBean.class, new ConnectAddonBeanDeserializer<M>(moduleListType))
                .disableHtmlEscaping()
                ;
    }

    public static <M extends ModuleList> Gson getGson(
            InstanceCreator<ModuleList> moduleListInstanceCreator
//            Class<M> moduleListType
    )
    {
        return getGsonBuilder(moduleListInstanceCreator).create();
    }

    public static GsonBuilder getGsonBuilder()
    {
        return getGsonBuilder(//JiraConfluenceModuleList.class,
                new InstanceCreator<ModuleList>()
        {
            @Override
            public ModuleList createInstance(Type type)
            {
                return new JiraConfluenceModuleList();
            }
        });
    }

    public static Gson getGson()
    {
        return getGsonBuilder().create();
    }

    public static ConnectAddonBean addonFromJsonWithI18nCollector(String json,Map<String,String> i18nCollector)
    {
        Gson gson;
        if(null != i18nCollector)
        {
            gson = getGsonBuilder().registerTypeAdapter(I18nProperty.class,new I18nCollectingDeserializer(i18nCollector)).create();
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
