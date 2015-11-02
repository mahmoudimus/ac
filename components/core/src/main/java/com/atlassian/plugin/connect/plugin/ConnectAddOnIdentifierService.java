package com.atlassian.plugin.connect.plugin;

/**
 * A publicly accessible service to identify if a given plugin is a connect add on
 */
public interface ConnectAddOnIdentifierService
{

    boolean isConnectAddOn(String pluginKey);
}
