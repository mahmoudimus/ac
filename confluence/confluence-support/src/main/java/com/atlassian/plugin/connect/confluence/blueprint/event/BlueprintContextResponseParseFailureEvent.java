package com.atlassian.plugin.connect.confluence.blueprint.event;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.event.api.AsynchronousPreferred;

/**
 *
 */
@EventName ("confluence.blueprint.context.response.parse.fail")
@AsynchronousPreferred
public class BlueprintContextResponseParseFailureEvent extends AbstractBlueprintContextEvent
{
    public BlueprintContextResponseParseFailureEvent(String addonKey, String moduleKey, String url)
    {
        super(addonKey, moduleKey, url);
    }
}
