package com.atlassian.plugin.connect.plugin.module;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.webresource.QueryParams;
import com.atlassian.plugin.webresource.condition.UrlReadingCondition;
import com.atlassian.plugin.webresource.url.UrlBuilder;

import java.util.Map;

/**
 * A {@link UrlReadingCondition} which returns true if there are Connect add-ons currently installed.
 */
public class ConnectAddonsInstalledCondition implements UrlReadingCondition
{
    private final ConnectAddonRegistry connectAddonRegistry;

    public ConnectAddonsInstalledCondition(final ConnectAddonRegistry connectAddonRegistry)
    {
        this.connectAddonRegistry = connectAddonRegistry;
    }

    @Override
    public void init(final Map<String, String> params) throws PluginParseException
    {
        // nothing to do
    }

    @Override
    public void addToUrl(final UrlBuilder urlBuilder)
    {
        urlBuilder.addToHash("hasConnectAddons", connectAddonRegistry.hasAddons());
    }

    @Override
    public boolean shouldDisplay(final QueryParams params)
    {
        return connectAddonRegistry.hasAddons();
    }
}
