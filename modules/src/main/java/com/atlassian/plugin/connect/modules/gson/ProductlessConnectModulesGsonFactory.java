package com.atlassian.plugin.connect.modules.gson;

import java.lang.reflect.Type;
import java.util.Map;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.JiraConfluenceModuleList;
import com.atlassian.plugin.connect.modules.beans.ModuleList;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.reflect.TypeToken;

/**
 * @since 1.0
 */
// useful for testing non product specific cases such as those that use common modules
public class ProductlessConnectModulesGsonFactory
{

    private static final InstanceCreator<ModuleList> MODULE_LIST_INSTANCE_CREATOR = new InstanceCreator<ModuleList>()
    {
        @Override
        public ModuleList createInstance(Type type)
        {
            return new ModuleList();
        }
    };

    private static final Type TYPE = new TypeToken<ConnectAddonBean<ModuleList>>() {}.getType();

    public static GsonBuilder getGsonBuilder()
    {
        return ConnectModulesGsonFactory.getGsonBuilder(MODULE_LIST_INSTANCE_CREATOR);
    }

    public static Gson getGson()
    {
        return getGsonBuilder().create();
    }

    public static String addonBeanToJson(ConnectAddonBean bean)
    {
        return getGson().toJson(bean);
    }

    public static <M extends ModuleList> ConnectAddonBean<M>
        addonFromJsonWithI18nCollector(String json, Map<String,String> i18nCollector)
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

        return gson.fromJson(json, TYPE);
    }
}
