package com.atlassian.plugin.connect.plugin.auth.scope;

import com.atlassian.analytics.api.annotations.EventName;

import javax.servlet.http.HttpServletRequest;

@EventName("connect.scoped.request.incoming.allowed")
public class ScopedRequestAllowedEvent extends ScopedRequestEvent {

    private static final long TRIMPRECISION = 100;

    private static final long THRESHOLD = 20 * 1000;

    private final int responseCode;

    private final long duration;

    public ScopedRequestAllowedEvent(HttpServletRequest rq, String addonKey, int responseCode, long duration) {
        super(rq, addonKey);
        this.responseCode = responseCode;
        this.duration = duration > THRESHOLD ? -1 : duration / TRIMPRECISION;
    }

    public long getDuration() {
        return this.duration;
    }

    public int getResponseCode() {
        return this.responseCode;
    }
}
