package com.atlassian.plugin.connect.confluence.blueprint.event;

import com.atlassian.analytics.api.annotations.EventName;

/**
 *
 */
@EventName ("confluence.blueprint.context.response.parse.fail")
public class BlueprintContextResponseParseFailureEvent extends AbstractBlueprintContextEvent
{
    public BlueprintContextResponseParseFailureEvent(String addonKey, String moduleKey, String url)
    {
        super(addonKey, moduleKey, url);
    }
}
