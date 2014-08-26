package com.atlassian.plugin.connect.modules.gson;

import java.lang.reflect.Type;
import java.util.List;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionType;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import static com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean.newCompositeConditionBean;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @since 1.0
 */
public class ConditionalBeanSerializer implements JsonSerializer<List<ConditionalBean>>, JsonDeserializer<List<ConditionalBean>>
{

    @Override
    public List<ConditionalBean> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        List<ConditionalBean> conditionalList = newArrayList();

        JsonArray ja = json.getAsJsonArray();

        for (int i = 0, size = ja.size(); i < size; i++)
        {
            JsonObject conditionalObject = ja.get(i).getAsJsonObject();

            if (conditionalObject.has("condition"))
            {
                conditionalList.add(context.<ConditionalBean>deserialize(conditionalObject, SingleConditionBean.class));
            }
            else if (conditionalObject.has("and"))
            {
                conditionalList.add(getCompositeCondition(context, CompositeConditionType.AND, conditionalObject));
            }
            else if (conditionalObject.has("or"))
            {
                conditionalList.add(getCompositeCondition(context, CompositeConditionType.OR, conditionalObject));
            }
        }

        return conditionalList;
    }

    @Override
    public JsonElement serialize(List<ConditionalBean> src, Type typeOfSrc, JsonSerializationContext context)
    {
        if (null == src || src.isEmpty())
        {
            return null;
        }

        Type conditionalType = new TypeToken<List<ConditionalBean>>() {}.getType();
        JsonArray ja = new JsonArray();

        for (ConditionalBean bean : src)
        {
            if (SingleConditionBean.class.isAssignableFrom(bean.getClass()))
            {
                ja.add(context.serialize(bean));
            }
            else if (CompositeConditionBean.class.isAssignableFrom(bean.getClass()))
            {
                CompositeConditionBean ccb = (CompositeConditionBean) bean;
                JsonObject obj = new JsonObject();
                obj.add(ccb.getType().name().toLowerCase(), context.serialize(ccb.getConditions(), conditionalType));
                ja.add(obj);
            }
        }

        return ja;
    }

    private CompositeConditionBean getCompositeCondition(JsonDeserializationContext context, CompositeConditionType type, JsonObject root)
    {
        String jsonTypeName = type.name().toLowerCase();
        JsonArray conditions = root.getAsJsonArray(jsonTypeName);
        root.remove(jsonTypeName);
        root.add("conditions", conditions);

        return newCompositeConditionBean(context.<CompositeConditionBean>deserialize(root, CompositeConditionBean.class)).withType(type).build();

    }
}
