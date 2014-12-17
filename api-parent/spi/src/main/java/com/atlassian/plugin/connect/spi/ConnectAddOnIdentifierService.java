package com.atlassian.plugin.connect.spi;

/**
 * A publicly accessible service to identify if a given plugin is a connect add on
 */
public interface ConnectAddOnIdentifierService
{
    public static final String CONNECT_ADDON_HEADER = "Atlassian-Connect-Addon";

    boolean isConnectAddOn(String pluginKey);
}
