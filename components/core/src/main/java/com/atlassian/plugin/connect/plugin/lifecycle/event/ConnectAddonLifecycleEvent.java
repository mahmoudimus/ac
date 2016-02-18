package com.atlassian.plugin.connect.plugin.lifecycle.event;

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
    private final String addonKey;

    protected ConnectAddonLifecycleEvent(String addonKey)
    {
        this.addonKey = checkNotNull(addonKey);
    }

    /**
     * Maintained for backwards compatibility. Will be removed in the future; please do not remove before May 2016.
     * @deprecated use {@link #getAddonKey()}
     * @return the add-on key as in the descriptor
     */
    public final String getPluginKey()
    {
        return addonKey;
    }

    /**
     * @return the add-on key as in the descriptor
     */
    public final String getAddonKey()
    {
        return addonKey;
    }
}
