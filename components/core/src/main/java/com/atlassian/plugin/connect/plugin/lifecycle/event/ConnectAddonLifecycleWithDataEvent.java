package com.atlassian.plugin.connect.plugin.lifecycle.event;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ConnectAddonLifecycleWithDataEvent extends ConnectAddonLifecycleEvent {
    /**
     * A string representation of (usually JSON) data that is sent along with the lifecycle
     * event. May contain add-on sensitive information such as the shared secret.
     */
    private final String data;

    protected ConnectAddonLifecycleWithDataEvent(String addonKey, String data) {
        super(addonKey);
        this.data = checkNotNull(data);
    }

    /**
     * @return The json data to be sent to the remote add on
     */
    public final String getData() {
        return data;
    }
}
