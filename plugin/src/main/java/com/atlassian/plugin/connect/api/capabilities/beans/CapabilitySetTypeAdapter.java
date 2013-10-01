package com.atlassian.plugin.connect.api.capabilities.beans;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.atlassian.plugin.connect.api.capabilities.annotation.CapabilitySet;

import com.google.common.base.Strings;
import com.google.gson.*;

import org.atteo.evo.classindex.ClassIndex;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

/**
 * A custom serializer / deserializer that can properly marshall the {@link DefaultCapabilitySetContainer}
 * This can handle modules as single objects or as arrays and will look up the proper module type using the
 * name of the object/array in the json and the scanned class index of CapabilityBeans.
 */
public class CapabilitySetTypeAdapter implements JsonSerializer, JsonDeserializer
{
    Map<String,Class<?>> labelToBean;
    
    public CapabilitySetTypeAdapter()
    {
        loadBeanMap();
    }

    private void loadBeanMap()
    {
        this.labelToBean = newHashMap();
        
        for(Class<?> clazz : ClassIndex.getAnnotated(CapabilitySet.class))
        {
            labelToBean.put(clazz.getAnnotation(CapabilitySet.class).key(),clazz);
        }
    }

    @Override
    public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject containerObj = json.getAsJsonObject();
        Gson g  = new Gson();
        DefaultCapabilitySetContainer container = g.fromJson(containerObj,DefaultCapabilitySetContainer.class);
        
        String collectionKey = getCollectionKey(containerObj);
        
        if(!Strings.isNullOrEmpty(collectionKey))
        {
            Class<?> clazz = labelToBean.get(collectionKey);
            List<CapabilityBean> lst = newArrayList();
            
            if(containerObj.get(collectionKey).isJsonArray())
            {
                JsonArray moduleArray = containerObj.getAsJsonArray(collectionKey);
                
                for(JsonElement el : moduleArray)
                {
                    lst.add((CapabilityBean) g.fromJson(el,clazz));
                }
            }
            else
            {
                lst.add((CapabilityBean) g.fromJson(containerObj.get(collectionKey),clazz));
            }
            
            container.setModules(lst);
        }
        
        return container;
    }

    private String getCollectionKey(JsonObject containerObj)
    {
        String collectionKey = "";
        for(String key : labelToBean.keySet())
        {
            if(containerObj.has(key))
            {
                collectionKey = key;
                break;
            }
        }
        
        return collectionKey;
    }

    @Override
    public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
