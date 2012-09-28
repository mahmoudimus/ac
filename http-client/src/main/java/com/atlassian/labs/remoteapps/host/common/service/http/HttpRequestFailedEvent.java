package com.atlassian.labs.remoteapps.host.common.service.http;

import com.atlassian.analytics.api.annotations.Analytics;

import java.util.Map;

@Analytics("plugin.httprequestfailed")
public class HttpRequestFailedEvent extends AbstractHttpRequestEvent
{
    public HttpRequestFailedEvent(String url, int statusCode, long elapsed, Map<String, String> properties)
    {
        super(url, statusCode, elapsed, properties);
    }

    public HttpRequestFailedEvent(String url, String error, long elapsed, Map<String, String> properties)
    {
        super(url, error, elapsed, properties);
    }
}
