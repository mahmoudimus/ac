package com.atlassian.plugin.connect.modules.gson;

import com.atlassian.plugin.connect.modules.beans.XmlDescriptorCodeInvokedEventBean;
import com.google.gson.*;

import java.lang.reflect.Type;

public class XmlDescriptorCodeInvokedEventBeanSerializer implements JsonSerializer<XmlDescriptorCodeInvokedEventBean>
{
    @Override
    public JsonElement serialize(XmlDescriptorCodeInvokedEventBean eventBean, Type type, JsonSerializationContext jsonSerializationContext)
    {
        JsonObject job = new JsonObject();
        job.addProperty("addOnKey", eventBean.getAddOnKey());
        job.add("stackTrace", buildStackTraceJsonArray(eventBean.getStackTrace()));
        return job;
    }

    private JsonArray buildStackTraceJsonArray(StackTraceElement[] stackTrace)
    {
        final JsonArray elements = new JsonArray();

        for (StackTraceElement element : stackTrace)
        {
            elements.add(new JsonPrimitive(element.toString()));
        }

        return elements;
    }
}
