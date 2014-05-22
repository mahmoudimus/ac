package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

@PrivacyPolicySafe
@EventName ("connect.request.incoming.failed")
public class ScopedRequestFailedEvent extends ScopedRequestEvent
{
    public ScopedRequestFailedEvent(String httpMethod, String httpRequestUri)
    {
        super(httpMethod, httpRequestUri);
    }
}
