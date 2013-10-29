package com.atlassian.plugin.connect.plugin.capabilities.gson;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.plugin.capabilities.beans.CapabilityBean;
import com.atlassian.plugin.osgi.util.BundleClassLoaderAccessor;

import com.google.gson.*;
import com.google.gson.internal.JsonReaderInternalAccess;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.atteo.evo.classindex.ClassIndex;
import org.osgi.framework.BundleContext;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

/**
 * @since 1.0
 */
public class CapabilityMapAdapterFactory implements TypeAdapterFactory
{
    final Map<String, TypeAdapter<CapabilityBean>> labelToDelegate;
    private final BundleContext bundleContext;

    public CapabilityMapAdapterFactory(BundleContext bundleContext)
    {
        this.bundleContext = bundleContext;
        labelToDelegate = new LinkedHashMap<String, TypeAdapter<CapabilityBean>>();
    }

    private void loadBeanMap(Gson gson)
    {
        if (labelToDelegate.isEmpty())
        {
            Iterable<Class<?>> capabilityClasses;
        
            //ClassIndex uses the current thread's context classloader, so we need to force it to be us.
            if(null != bundleContext)
            {
                ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(BundleClassLoaderAccessor.getClassLoader(bundleContext.getBundle(),null));
                
                capabilityClasses = ClassIndex.getAnnotated(CapabilitySet.class);
                
                Thread.currentThread().setContextClassLoader(oldCl);
            } 
            else
            {
                capabilityClasses = ClassIndex.getAnnotated(CapabilitySet.class);
            }
            
            for (Class<?> clazz : capabilityClasses)
            {
                TypeAdapter delegate = gson.getDelegateAdapter(this, TypeToken.get(clazz));

                labelToDelegate.put(clazz.getAnnotation(CapabilitySet.class).key(), delegate);
            }
        }
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken)
    {
        Type mapType = typeToken.getType();
        Class<?> rawType = typeToken.getRawType();

        if (!Map.class.isAssignableFrom(rawType) || !(mapType instanceof ParameterizedType))
        {
            return null;
        }
        ParameterizedType mapParameterizedType = (ParameterizedType) mapType;
        Type[] keyAndValueTypes = mapParameterizedType.getActualTypeArguments();

        if (keyAndValueTypes.length < 2 
                || !keyAndValueTypes[0].equals(String.class) 
                || !ParameterizedType.class.isAssignableFrom(keyAndValueTypes[1].getClass()))
        {
            return null;
        }

        ParameterizedType listType = (ParameterizedType) keyAndValueTypes[1];
        Type[] listTypeParams = listType.getActualTypeArguments();

        if (!listType.getRawType().equals(List.class) 
                || listTypeParams.length != 1 
                || !WildcardType.class.isAssignableFrom(listTypeParams[0].getClass()))
        {
            return null;
        }

        WildcardType wildcardType = (WildcardType) listTypeParams[0];
        
        if(wildcardType.getUpperBounds().length != 1
                || !wildcardType.getUpperBounds()[0].equals(CapabilityBean.class))
        {
            return null;
        }
        
        
        loadBeanMap(gson);

        return (TypeAdapter<T>) new CapabilityMapAdapter(labelToDelegate).nullSafe();
    }

    private class CapabilityMapAdapter extends TypeAdapter<Map<String, List<CapabilityBean>>>
    {
        private final Map<String, TypeAdapter<CapabilityBean>> labelToDelegate;

        public CapabilityMapAdapter(Map<String, TypeAdapter<CapabilityBean>> labelToDelegate)
        {
            this.labelToDelegate = labelToDelegate;
        }

        @Override
        public void write(JsonWriter out, Map<String, List<CapabilityBean>> capabilityMap) throws IOException
        {
            out.beginObject();

            for(Map.Entry<String, List<CapabilityBean>> entry : capabilityMap.entrySet())
            {
                String label = entry.getKey();
                TypeAdapter delegate = labelToDelegate.get(label);

                if (delegate == null) 
                {
                    throw new JsonParseException("cannot serialize capability '" + label + "'; did you annotate the bean class?");
                }
                
                out.name(label);
                
                List<CapabilityBean> configEntryList = entry.getValue();
                
                if(configEntryList.size() < 2)
                {
                    delegate.write(out, configEntryList.get(0));
                }
                else
                {
                    out.beginArray();
                    
                    for(CapabilityBean bean : configEntryList)
                    {
                        delegate.write(out,bean);
                    }
                    
                    out.endArray();
                }
            }

            out.endObject();
        }

        @Override
        public Map<String, List<CapabilityBean>> read(JsonReader in) throws IOException
        {
            Map<String, List<CapabilityBean>> capabilityMap = newHashMap();
            
            in.beginObject();
            
            while(in.hasNext())
            {
                JsonReaderInternalAccess.INSTANCE.promoteNameToValue(in);
                JsonElement jsonElement = Streams.parse(in);
                String label = jsonElement.getAsString();

                TypeAdapter<CapabilityBean> delegate = labelToDelegate.get(label);
                
                if(null == delegate)
                {
                    //just eat it
                    Streams.parse(in);
                }
                else
                {
                    List<CapabilityBean> beanList = newArrayList();
                    if(JsonToken.BEGIN_ARRAY == in.peek())
                    {
                        in.beginArray();
                        
                        while(in.hasNext())
                        {
                            beanList.add(delegate.read(in));
                        }
                        in.endArray();
                        capabilityMap.put(label,beanList);
                    }
                    else
                    {
                        beanList.add(delegate.read(in));
                        capabilityMap.put(label,beanList);
                    }
                }
            }
            
            in.endObject();
            
            return capabilityMap;
        }
    }
}
