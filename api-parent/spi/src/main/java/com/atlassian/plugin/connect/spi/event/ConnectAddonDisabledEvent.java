package com.atlassian.plugin.connect.spi.event;

public class ConnectAddonDisabledEvent extends ConnectAddonEvent
{
    public ConnectAddonDisabledEvent(String pluginKey, String data)
    {
        super(pluginKey, data);
    }
}
