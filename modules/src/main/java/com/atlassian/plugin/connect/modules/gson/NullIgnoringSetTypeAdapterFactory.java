package com.atlassian.plugin.connect.modules.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class NullIgnoringSetTypeAdapterFactory implements TypeAdapterFactory
{

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken)
    {
        Type type = typeToken.getType();
        Class rawType = typeToken.getRawType();
        
        if(rawType != Set.class || !(type instanceof ParameterizedType))
        {
            return null;   
        }
        
        Type nestedType = ((ParameterizedType) type).getActualTypeArguments()[0];
        TypeAdapter nestedTypeAdapter = gson.getAdapter(TypeToken.get(nestedType));
        
        return (TypeAdapter<T>) new NullIgnoringSetTypeAdapter<T>(nestedTypeAdapter).nullSafe();
        
    }

    private class NullIgnoringSetTypeAdapter<I> extends TypeAdapter<Set<I>>
    {
        private final TypeAdapter<I> nestedAdapter;

        private NullIgnoringSetTypeAdapter(TypeAdapter<I> nestedAdapter)
        {
            this.nestedAdapter = nestedAdapter;
        }

        @Override
        public void write(JsonWriter out, Set<I> value) throws IOException
        {
            if (null == value)
            {
                out.nullValue();
            }
            else
            {
                out.beginArray();
                
                for(I item : value)
                {
                    nestedAdapter.write(out,item);
                }
                
                out.endArray();
            }
        }

        @Override
        public Set<I> read(JsonReader in) throws IOException
        {
            Set<I> theSet = new HashSet<I>();
            
            in.beginArray();
            
            while(in.hasNext())
            {
                I item = nestedAdapter.read(in);
                
                if(null != item)
                {
                    theSet.add(item);
                }
            }
            in.endArray();
            
            return theSet;
        }
    }
}
