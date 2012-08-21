package com.atlassian.labs.remoteapps.host.common.service.http;

import java.util.Map;

public abstract class AbstractHttpRequestEvent
{
    private String url;
    private int statusCode;
    private String error;
    private long elapsed;
    private Map<String, String> properties;

    public AbstractHttpRequestEvent(String url, int statusCode, long elapsed, Map<String, String> properties)
    {
        this.url = url;
        this.statusCode = statusCode;
        this.elapsed = elapsed;
        this.properties = properties;
    }

    public AbstractHttpRequestEvent(String url, String error, long elapsed, Map<String, String> properties)
    {
        this.url = url;
        this.error = error;
        this.elapsed = elapsed;
        this.properties = properties;
    }

    public String getUrl()
    {
        return url;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public String getError()
    {
        return error;
    }

    public long getElapsed()
    {
        return elapsed;
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }
}
