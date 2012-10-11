package com.atlassian.plugin.remotable.host.common.service.http;

import com.atlassian.analytics.api.annotations.Analytics;

import java.util.Map;

@Analytics("plugin.httprequestcompleted")
public class HttpRequestCompletedEvent extends AbstractHttpRequestEvent
{
    public HttpRequestCompletedEvent(String url, int statusCode, long elapsed, Map<String, String> properties)
    {
        super(url, statusCode, elapsed, properties);
    }
}
