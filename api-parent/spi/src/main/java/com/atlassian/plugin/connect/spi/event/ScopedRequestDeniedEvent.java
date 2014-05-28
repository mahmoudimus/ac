package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

@PrivacyPolicySafe
@EventName ("connect.scoped.request.incoming.denied")
public class ScopedRequestDeniedEvent extends ScopedRequestEvent
{
    public ScopedRequestDeniedEvent(String httpMethod, String httpRequestUri)
    {
        super(httpMethod, httpRequestUri);
    }
}
