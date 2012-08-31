package com.atlassian.labs.remoteapps.plugin.product;

import org.json.JSONException;

import java.util.Map;

public interface EventMapper<T>
{
    public boolean handles(T e);

    public Map<String, Object> toMap(T e) throws JSONException;
}
