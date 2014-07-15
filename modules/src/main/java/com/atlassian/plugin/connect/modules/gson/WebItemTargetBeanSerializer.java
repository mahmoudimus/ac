package com.atlassian.plugin.connect.modules.gson;

import java.lang.reflect.Type;

import com.atlassian.plugin.connect.modules.beans.WebItemTargetBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.builder.WebItemTargetBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.DialogOptions;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.InlineDialogOptions;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.WebItemTargetOptions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class WebItemTargetBeanSerializer implements JsonDeserializer<WebItemTargetBean>
{
    @Override
    public WebItemTargetBean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException
    {
        final WebItemTargetBeanBuilder builder = WebItemTargetBean.newWebItemTargetBean();
        final JsonObject webItemTargetJson = json.getAsJsonObject();
        final WebItemTargetType type = context.<WebItemTargetType>deserialize(webItemTargetJson.get("type"),
                WebItemTargetType.class);
        builder.withType(type);

        Class<? extends WebItemTargetOptions> optionsType = null;

        switch (type)
        {
            case inlineDialog:
                optionsType = InlineDialogOptions.class;
                break;

            case dialog:
                optionsType = DialogOptions.class;
                break;
        }

        if (optionsType != null)
        {
            builder.withOptions(context.<WebItemTargetOptions>deserialize(webItemTargetJson.get("options"), optionsType));
        }
        return builder.build();
    }
}
