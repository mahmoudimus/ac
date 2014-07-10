package com.atlassian.plugin.connect.plugin.applinks;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

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
     * @param addonUserKey the user-key of the add-on user; will be stored for later retrieval when we work out the {@link java.security.Principal} for incoming requests from this add-on
     * 
     * @deprecated use the addonBean version
     */
    @Deprecated //use the addonBean version
    void createAppLink(Plugin plugin, String baseUrl, AuthenticationType authType, String publicKey, String addonUserKey);

    /**
     * Creates an {@link ApplicationLink} for a connect add-on.
     * @param addon The addon to create the applink for
     * @param baseUrl The baseurl of the connect addon
     * @param authType JWT or OAUTH
     * @param publicKey the publicKey used for asymmetric key encryption. Cannot be null if using OAUTH or JWT+RSA
     * @param addonUserKey the user-key of the add-on user; will be stored for later retrieval when we work out the {@link java.security.Principal} for incoming requests from this add-on
     */
    void createAppLink(ConnectAddonBean addon, String baseUrl, AuthenticationType authType, String publicKey, String addonUserKey);

    /**
     * Deletes an {@link ApplicationLink} for an Atlassian Connect add-on.
     * @param addon the addon to delete the applink for
     */
    void deleteAppLink(ConnectAddonBean addon) throws NotConnectAddonException;

    /**
     * Retrieves an {@link ApplicationLink} found for the Atlassian Connect add-on with given key
     * @param key plugin key for the connect add-on
     * @return the {@link ApplicationLink}, or null if either there are none or the plugin key is not associated with a Connect add-on
     * @throws com.atlassian.plugin.connect.plugin.applinks.NotConnectAddonException if the key belongs to a plugin which is not a Connect add-on
     */
    public ApplicationLink getAppLink(String key) throws NotConnectAddonException;
}
