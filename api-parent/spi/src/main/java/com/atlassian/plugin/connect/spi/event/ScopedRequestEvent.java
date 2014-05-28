package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

@PrivacyPolicySafe
public abstract class ScopedRequestEvent
{
    @PrivacyPolicySafe
    private final String httpMethod;

    @PrivacyPolicySafe
    private final String httpRequestUri;

    public ScopedRequestEvent(String httpMethod, String httpRequestUri)
    {
        this.httpMethod = httpMethod;
        this.httpRequestUri = httpRequestUri;
    }

    public String getHttpMethod()
    {
        return httpMethod;
    }

    public String getHttpRequestUri()
    {
        return httpRequestUri;
    }
}
