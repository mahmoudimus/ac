package com.atlassian.plugin.connect.modules.gson;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ModuleListDeserializer<T extends BaseModuleBean> implements JsonDeserializer<List<T>>
{
    private Class<T> beanClass;

    public ModuleListDeserializer(Class<T> beanClass)
    {
        this.beanClass = beanClass;
    }

    @Override
    public List<T> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException
    {
        JsonArray array = jsonElement.getAsJsonArray();
        List<T> beanList = new ArrayList<>();
        for (int i = 0; i < array.size(); i++)
        {
            T bean = jsonDeserializationContext.deserialize(array.get(i), beanClass);
            beanList.add(bean);
        }

        return beanList;
    }
}
