package com.atlassian.plugin.connect.confluence.blueprint.event;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.event.api.AsynchronousPreferred;

/**
 *
 */
@EventName("confluence.blueprint.context.response.parse.success")
@AsynchronousPreferred
public class BlueprintContextResponseParseSuccessEvent extends AbstractBlueprintContextEvent {
    private final long timeTakenMillis;

    public BlueprintContextResponseParseSuccessEvent(String addonKey, String moduleKey, String url, long timeTakenMillis) {
        super(addonKey, moduleKey, url);
        this.timeTakenMillis = timeTakenMillis;
    }

    public long getTimeTakenMillis() {
        return timeTakenMillis;
    }
}
