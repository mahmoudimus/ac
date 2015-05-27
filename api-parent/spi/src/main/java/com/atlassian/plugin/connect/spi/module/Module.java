package com.atlassian.plugin.connect.spi.module;

import com.atlassian.jira.util.json.JSONObject;

public class Module
{
    private final JSONObject jsonObject;
    public Module(JSONObject jsonObject)
    {
        this.jsonObject = jsonObject;
    }
    public <T> T toBean(Class<T> clazz) {
        return null;
        //return convert(jsonObject);
    }
}
