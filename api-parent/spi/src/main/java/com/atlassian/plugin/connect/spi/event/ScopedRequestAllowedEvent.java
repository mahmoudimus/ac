package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

@PrivacyPolicySafe
@EventName ("connect.request.incoming.allowed")
public class ScopedRequestAllowedEvent extends ScopedRequestEvent
{
    public ScopedRequestAllowedEvent(String httpMethod, String httpRequestUri)
    {
        super(httpMethod, httpRequestUri);
    }
}
