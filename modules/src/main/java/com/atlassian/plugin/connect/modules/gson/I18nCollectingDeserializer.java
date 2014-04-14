package com.atlassian.plugin.connect.modules.gson;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.google.common.base.Strings;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Map;

public class I18nCollectingDeserializer implements JsonDeserializer<I18nProperty>
{
    private final Map<String,String> i18nCollector;
    
    public I18nCollectingDeserializer(Map<String, String> i18nCollector) 
    {
        this.i18nCollector = i18nCollector;
    }

    @Override
    public I18nProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        I18nProperty prop = ConnectModulesGsonFactory.getGson().fromJson(json,I18nProperty.class);
        
        if(!Strings.isNullOrEmpty(prop.getI18n()))
        {
            i18nCollector.put(prop.getI18n(),prop.getValue());
        }
        
        return prop;
    }
}
