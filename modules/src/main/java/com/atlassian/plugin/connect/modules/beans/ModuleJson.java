package com.atlassian.plugin.connect.modules.beans;

import com.google.gson.JsonObject;

public class ModuleJson
{
    private final JsonObject jsonObject;
    public ModuleJson(JsonObject jsonObject)
    {
        this.jsonObject = jsonObject;
    }
    public <T> T toBean(Class<T> clazz) {
        return null;
        //return convert(jsonObject);
    }
}
