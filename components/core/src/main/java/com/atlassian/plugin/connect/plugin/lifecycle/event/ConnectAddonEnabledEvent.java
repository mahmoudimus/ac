package com.atlassian.plugin.connect.plugin.lifecycle.event;

import com.atlassian.analytics.api.annotations.EventName;

/**
 * Fired when the remote application's mirror plugin is enabled
 */
@EventName("connect.addon.enabled")
public class ConnectAddonEnabledEvent extends ConnectAddonLifecycleWithDataEvent {
    public ConnectAddonEnabledEvent(String addonKey, String data) {
        super(addonKey, data);
    }
}
