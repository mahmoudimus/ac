package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

@PrivacyPolicySafe
public abstract class ScopedRequestEvent
{

    private static final long TRIMPRECISION = 100;

    private static final long THRESHOLD = 20 * 1000;

    @PrivacyPolicySafe
    private final String httpMethod;

    @PrivacyPolicySafe
    private final String httpRequestUri;

    @PrivacyPolicySafe
    private final int responseCode;

    @PrivacyPolicySafe
    private final long duration;

    public ScopedRequestEvent(String httpMethod, String httpRequestUri, int responseCode, long duration)
    {
        super();
        this.httpMethod = httpMethod;
        this.httpRequestUri = httpRequestUri;
        this.responseCode = responseCode;
        this.duration = duration > THRESHOLD ? -1 : duration / TRIMPRECISION;
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
