package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This is the base class for all connect add-on events.
 * Every event carries the pluginKey of the add-on's mirror plugin.
 * 
 * The actual subscribing to these events from the remote application's
 * perspective is done via a lifecycle entry in the json descriptor.
 */
public abstract class ConnectAddonLifecycleEvent
{
    @PrivacyPolicySafe
    private final String pluginKey;

    protected ConnectAddonLifecycleEvent(String pluginKey)
    {
        this.pluginKey = checkNotNull(pluginKey);
    }

    public final String getPluginKey()
    {
        return pluginKey;
    }
}
