package com.atlassian.plugin.connect.plugin.capabilities.gson;

import java.lang.reflect.Type;

import com.atlassian.plugin.connect.plugin.capabilities.beans.AuthenticationBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AuthenticationType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.AuthenticationBeanBuilder;

import com.google.gson.*;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.AuthenticationBean.newAuthenticationBean;

public class AuthenticationSerializer implements JsonSerializer<AuthenticationBean>, JsonDeserializer<AuthenticationBean>
{
    public static final String TYPE_FIELD = "type";
    public static final String KEY_FIELD = "sharedKey";
    
    @Override
    public AuthenticationBean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject authObject = json.getAsJsonObject();
        AuthenticationBeanBuilder builder = newAuthenticationBean();
        
        if(authObject.has(TYPE_FIELD))
        {
            builder.withType(AuthenticationType.valueOf(authObject.get(TYPE_FIELD).getAsString().toUpperCase()));
        }
        builder.withSharedKey(authObject.get(KEY_FIELD).getAsString());
        
        return builder.build();
    }

    @Override
    public JsonElement serialize(AuthenticationBean authBean, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject job = new JsonObject();
        job.addProperty(KEY_FIELD,authBean.getSharedKey());
        job.addProperty(TYPE_FIELD,authBean.getType().name().toLowerCase());
        
        return job;
    }
}
