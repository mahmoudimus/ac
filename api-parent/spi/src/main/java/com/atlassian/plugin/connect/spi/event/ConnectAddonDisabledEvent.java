package com.atlassian.plugin.connect.spi.event;

/**
 * Fired when the remote application's mirror plugin is disabled
 */
public class ConnectAddonDisabledEvent extends ConnectAddonEvent
{
    public ConnectAddonDisabledEvent(String pluginKey, String data)
    {
        super(pluginKey, data);
    }
}
