package com.atlassian.plugin.connect.spi.event;

/**
 * Fired when the remote application's mirror plugin is enabled
 */
public class ConnectAddonEnabledEvent extends ConnectAddonEvent
{
    public ConnectAddonEnabledEvent(String pluginKey, String data)
    {
        super(pluginKey, data);
    }
}
