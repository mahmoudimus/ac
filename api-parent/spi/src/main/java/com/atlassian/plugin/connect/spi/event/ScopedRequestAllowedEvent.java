package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

@PrivacyPolicySafe
@EventName("connect.scoped.request.incoming.allowed")
public class ScopedRequestAllowedEvent extends ScopedRequestEvent
{

    private static final long TRIMPRECISION = 100;

    private static final long THRESHOLD = 20 * 1000;

    @PrivacyPolicySafe
    private final int responseCode;

    @PrivacyPolicySafe
    private final long duration;

    public ScopedRequestAllowedEvent(String httpMethod, String httpRequestUri, int responseCode, long duration)
    {
        super(httpMethod, httpRequestUri);
        this.responseCode = responseCode;
        this.duration = duration > THRESHOLD ? -1 : duration / TRIMPRECISION;
    }

    public long getDuration()
    {
        return this.duration;
    }

    public int getResponseCode()
    {
        return this.responseCode;
    }
}
