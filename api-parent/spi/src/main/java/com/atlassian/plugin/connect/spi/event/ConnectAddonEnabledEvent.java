package com.atlassian.plugin.connect.spi.event;

public class ConnectAddonEnabledEvent extends ConnectAddonEvent
{
    public ConnectAddonEnabledEvent(String pluginKey, String data)
    {
        super(pluginKey, data);
    }
}
