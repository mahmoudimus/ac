package com.atlassian.plugin.connect.confluence.blueprint.event;

import com.atlassian.analytics.api.annotations.EventName;

/**
 *
 */
@EventName ("confluence.blueprint.context.request.fail")
public class BlueprintContextRequestFailedEvent extends AbstractBlueprintContextEvent
{
    private final String failureType;

    public BlueprintContextRequestFailedEvent(String addonKey, String moduleKey, String url, String failureType) {
        super(addonKey, moduleKey, url);
        this.failureType = failureType;
    }

    public String getFailureType()
    {
        return failureType;
    }
}
