package com.atlassian.plugin.connect.plugin.auth.scope;

import com.atlassian.analytics.api.annotations.EventName;

import javax.servlet.http.HttpServletRequest;

@EventName("connect.scoped.request.incoming.denied")
public class ScopedRequestDeniedEvent extends ScopedRequestEvent {
    public ScopedRequestDeniedEvent(HttpServletRequest rq, String addonKey) {
        super(rq, addonKey);
    }
}
