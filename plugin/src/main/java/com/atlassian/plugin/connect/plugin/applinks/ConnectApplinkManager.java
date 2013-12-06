package com.atlassian.plugin.connect.plugin.applinks;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AuthenticationType;

/**
 * A helper component for creating applinks for add ons.
 */
public interface ConnectApplinkManager
{
    /**
     * Creates an {@link ApplicationLink} for a connect add-on.
     * @param plugin The plugin to create the applink for
     * @param baseUrl The baseurl of the connect addon
     * @param authType JWT or OAUTH
     * @param signingKey either the JWT shared secret, JWT public key or the OAUTH publicKey depending on auth type and algorithm
     */
    void createAppLink(Plugin plugin, String baseUrl, AuthenticationType authType, String signingKey);

    /**
     * Deletes an {@link ApplicationLink} for an Atlassian Connect add-on.
     * @param plugin the plugin to delete the applink for
     */
    void deleteAppLink(Plugin plugin) throws NotConnectAddonException;

    /**
     * Retrieves an {@link ApplicationLink} found for the Atlassian Connect add-on with given key
     * @param key plugin key for the connect add-on
     * @return the {@link ApplicationLink}, or null if either there are none or the plugin key is not associated with a Connect add-on
     * @throws com.atlassian.plugin.connect.plugin.applinks.NotConnectAddonException if the key belongs to a plugin which is not a Connect add-on
     */
    public ApplicationLink getAppLink(String key) throws NotConnectAddonException;
}
