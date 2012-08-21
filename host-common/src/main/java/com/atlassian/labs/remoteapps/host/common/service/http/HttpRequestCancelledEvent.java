package com.atlassian.labs.remoteapps.host.common.service.http;

import com.atlassian.analytics.api.annotations.Analytics;

import java.util.Map;

@Analytics("plugin.httprequestcancelled")
public class HttpRequestCancelledEvent extends AbstractHttpRequestEvent
{
    public HttpRequestCancelledEvent(String url, String error, long elapsed, Map<String,String> properties)
    {
        super(url, error, elapsed, properties);
    }
}
