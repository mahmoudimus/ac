package com.atlassian.plugin.connect.plugin.module;

import com.atlassian.plugin.connect.api.registry.ConnectAddonRegistry;
import com.atlassian.plugin.webresource.condition.SimpleUrlReadingCondition;
import com.atlassian.plugin.webresource.condition.UrlReadingCondition;

/**
 * A {@link UrlReadingCondition} which returns true if there are Connect add-ons currently installed.
 */
public class ConnectAddonsInstalledCondition extends SimpleUrlReadingCondition
{
    private final ConnectAddonRegistry connectAddonRegistry;

    public ConnectAddonsInstalledCondition(final ConnectAddonRegistry connectAddonRegistry)
    {
        this.connectAddonRegistry = connectAddonRegistry;
    }

    @Override
    protected boolean isConditionTrue()
    {
        return connectAddonRegistry.hasAddons();
    }

    @Override
    protected String queryKey()
    {
        return "hasConnectAddons";
    }
}
