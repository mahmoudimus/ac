package com.atlassian.plugin.connect.plugin.applinks;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;

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
     * @param publicKey the publicKey used for asymmetric key encryption. Cannot be null if using OAUTH or JWT+RSA
     */
    void createAppLink(Plugin plugin, String baseUrl, AuthenticationType authType, String publicKey);

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
