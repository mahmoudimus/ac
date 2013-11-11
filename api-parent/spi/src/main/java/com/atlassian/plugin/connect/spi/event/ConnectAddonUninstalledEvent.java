package com.atlassian.plugin.connect.spi.event;

public class ConnectAddonUninstalledEvent extends ConnectAddonEvent
{
    public ConnectAddonUninstalledEvent(String pluginKey, String data)
    {
        super(pluginKey, data);
    }
}
