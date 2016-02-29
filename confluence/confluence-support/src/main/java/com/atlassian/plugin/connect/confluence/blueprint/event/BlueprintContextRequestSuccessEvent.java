package com.atlassian.plugin.connect.confluence.blueprint.event;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.event.api.AsynchronousPreferred;

/**
 *
 */

@EventName("confluence.blueprint.context.request.success")
@AsynchronousPreferred
public class BlueprintContextRequestSuccessEvent extends AbstractBlueprintContextEvent {
    private final long timeTakenMillis;

    public BlueprintContextRequestSuccessEvent(String addonKey, String moduleKey, String url, long timeTakenMillis) {
        super(addonKey, moduleKey, url);
        this.timeTakenMillis = timeTakenMillis;
    }

    public long getTimeTakenMillis() {
        return timeTakenMillis;
    }
}
