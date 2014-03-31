package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This is the base class for all json-based connect plugin events.
 * These events represent things the remote application can listen for.
 * Every event carries the pluginKey of the remote application's mirror plugin and the json formatted data to send to it
 * 
 * Most of these events will be registered as webhooks.
 * NOTE: these events are only used internally. The actualy subscribing to these events from the remote application's
 * perspective is done via a lifecycle entry in the json descriptor
 */
public abstract class ConnectAddonLifecycleWithDataEvent extends ConnectAddonLifecycleEvent
{
    @PrivacyPolicySafe (false) // contains shared secret and more
    private final String data;

    protected ConnectAddonLifecycleWithDataEvent(String pluginKey, String data)
    {
        super(pluginKey);
        this.data = checkNotNull(data);
    }

    /**
     * @return The json data to be sent to the remote add on
     */
    public final String getData()
    {
        return data;
    }
}
