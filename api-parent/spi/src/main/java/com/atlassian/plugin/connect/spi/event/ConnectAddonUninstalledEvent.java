package com.atlassian.plugin.connect.spi.event;

/**
 * Fired when the remote application's mirror plugin is uninstalled
 */
public class ConnectAddonUninstalledEvent extends ConnectAddonEvent
{
    public ConnectAddonUninstalledEvent(String pluginKey, String data)
    {
        super(pluginKey, data);
    }
}
