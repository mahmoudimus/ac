package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AuthenticationType;

/**
 * A helper component for creating applinks for add ons.
 */
public interface ConnectApplinkManager
{
    /**
     * Creates an applink for a connect add on.
     * @param plugin The plugin to create the applink for
     * @param baseUrl The baseurl of the connect addon
     * @param authType JWT or OAUTH
     * @param sharedKey either the JWT shared secret or the OAUTH publicKey depending on auth type
     */
    void createAppLink(Plugin plugin, String baseUrl, AuthenticationType authType, String sharedKey);
}
