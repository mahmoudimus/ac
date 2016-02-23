package com.atlassian.plugin.connect.modules.gson;


import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class LifecycleSerializer implements JsonSerializer<LifecycleBean> {

    @Override
    public JsonElement serialize(LifecycleBean src, Type typeOfSrc, JsonSerializationContext context) {
        if (src.isEmpty()) {
            return null;
        }

        JsonObject job = new JsonObject();

        if (!Strings.isNullOrEmpty(src.getInstalled())) {
            job.addProperty("installed", src.getInstalled());
        }
        if (!Strings.isNullOrEmpty(src.getUninstalled())) {
            job.addProperty("uninstalled", src.getUninstalled());
        }
        if (!Strings.isNullOrEmpty(src.getEnabled())) {
            job.addProperty("enabled", src.getEnabled());
        }
        if (!Strings.isNullOrEmpty(src.getDisabled())) {
            job.addProperty("disabled", src.getDisabled());
        }

        return job;
    }
}
