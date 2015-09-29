package com.atlassian.plugin.connect.modules.gson;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionType;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConditionalBeanTypeAdapterFactory implements TypeAdapterFactory
{
    private final Type conditionalListType;

    public ConditionalBeanTypeAdapterFactory(Type conditionalListType)
    {
        this.conditionalListType = conditionalListType;
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken)
    {
        Class rawType = typeToken.getRawType();

        if(rawType != ConditionalBean.class && rawType != SingleConditionBean.class && rawType != CompositeConditionBean.class)
        {
            return null;
        }

        return getTypeAdapter(gson);
    }

    @SuppressWarnings("unchecked")
    private <T> TypeAdapter<T> getTypeAdapter(Gson gson)
    {
        return (TypeAdapter<T>) new ConditionalBeanSerializer(gson);
    }

    private class ConditionalBeanSerializer extends TypeAdapter<ConditionalBean>
    {
        private final Gson gson;

        public ConditionalBeanSerializer(Gson gson)
        {
            this.gson = gson;
        }

        @Override
        public void write(JsonWriter jsonWriter, ConditionalBean conditionalBean) throws IOException
        {
            if (SingleConditionBean.class.isAssignableFrom(conditionalBean.getClass()))
            {
                writeConditionBean(jsonWriter, (SingleConditionBean) conditionalBean);
            }
            else if (CompositeConditionBean.class.isAssignableFrom(conditionalBean.getClass()))
            {
                writeConditionBean(jsonWriter, (CompositeConditionBean) conditionalBean);
            }
        }

        private void writeConditionBean(JsonWriter jsonWriter, CompositeConditionBean compositeConditionBean) throws IOException
        {
            jsonWriter.beginObject()
                    .name(compositeConditionBean.getType().toString().toLowerCase())
                    .beginArray();

            for (ConditionalBean subBean : compositeConditionBean.getConditions())
            {
                write(jsonWriter, subBean);
            }

            jsonWriter.endArray()
                    .endObject();
        }

        private void writeConditionBean(JsonWriter jsonWriter, SingleConditionBean singleConditionBean) throws IOException
        {
            final Map<String, String> params = singleConditionBean.getParams();
            jsonWriter.beginObject()
                    .name("condition").value(singleConditionBean.getCondition())
                    .name("invert").value(singleConditionBean.isInvert());

            if (params != null && !params.isEmpty())
            {
                writeParams(jsonWriter, params);
            }

            jsonWriter.endObject();
        }

        private void writeParams(JsonWriter jsonWriter, Map<String, String> params) throws IOException
        {
            jsonWriter.name("params");
            jsonWriter.beginObject();

            for (Map.Entry<String, String> param : params.entrySet())
            {
                jsonWriter.name(param.getKey()).value(param.getValue());
            }

            jsonWriter.endObject();
        }

        @Override
        public ConditionalBean read(JsonReader jsonReader) throws IOException
        {
            ConditionalBean conditionalBean = null;
            jsonReader.beginObject();

            // ugly collection of properties required because we are reading disjoint sets of properties into ConditionalBean subtypes
            boolean invert = false;
            String conditionName = null;
            CompositeConditionType compositeConditionType = CompositeConditionType.AND;
            List<ConditionalBean> compositeBeans = Collections.emptyList();
            Map<String, String> params = Collections.emptyMap();

            while (!jsonReader.peek().equals(JsonToken.END_OBJECT))
            {
                String nextPropertyName = jsonReader.nextName();

                if ("conditions".equals(nextPropertyName))
                {
                    compositeBeans = gson.fromJson(jsonReader, conditionalListType);
                }
                else if ("and".equals(nextPropertyName))
                {
                    compositeConditionType = CompositeConditionType.AND;
                    compositeBeans = gson.fromJson(jsonReader, conditionalListType);
                }
                else if ("or".equals(nextPropertyName))
                {
                    compositeConditionType = CompositeConditionType.OR;
                    compositeBeans = gson.fromJson(jsonReader, conditionalListType);
                }
                else if ("condition".equals(nextPropertyName))
                {
                    conditionName = jsonReader.nextString();
                }
                else if ("invert".equals(nextPropertyName))
                {
                    // the strings "true" and "false" have historically been successfully parsed
                    if (jsonReader.peek().equals(JsonToken.STRING))
                    {
                        invert = Boolean.valueOf(jsonReader.nextString());
                    }
                    else
                    {
                        invert = jsonReader.nextBoolean();
                    }
                }
                else if ("params".equals(nextPropertyName))
                {
                    params = gson.fromJson(jsonReader, new TypeToken<Map<String, String>>(){}.getType());
                }
                else
                {
                    jsonReader.skipValue(); // Be conservative in what you send; be liberal in what you accept.
                }
            }

            jsonReader.endObject();

            if (null != conditionName)
            {
                conditionalBean = SingleConditionBean.newSingleConditionBean()
                    .withCondition(conditionName)
                    .withInvert(invert)
                    .withParams(params)
                    .build();
            }
            else
            {
                conditionalBean = CompositeConditionBean.newCompositeConditionBean()
                    .withConditions(compositeBeans)
                    .withType(compositeConditionType)
                    .build();
            }

            return conditionalBean;
        }
    }
}
