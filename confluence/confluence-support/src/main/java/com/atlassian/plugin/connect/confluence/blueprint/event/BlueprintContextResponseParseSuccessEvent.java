package com.atlassian.plugin.connect.confluence.blueprint.event;

import com.atlassian.analytics.api.annotations.EventName;

/**
 *
 */
@EventName ("confluence.blueprint.context.response.parse.success")
public class BlueprintContextResponseParseSuccessEvent extends AbstractBlueprintContextEvent
{
    private final long timeTakenMillis;

    public BlueprintContextResponseParseSuccessEvent(String addonKey, String moduleKey, String url, long timeTakenMillis)
    {
        super(addonKey, moduleKey, url);
        this.timeTakenMillis = timeTakenMillis;
    }

    public long getTimeTakenMillis()
    {
        return timeTakenMillis;
    }
}
