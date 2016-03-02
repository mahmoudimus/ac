package com.atlassian.plugin.connect.plugin.auth.applinks;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

import java.net.URI;
import java.util.Optional;

/**
 * A helper component for creating applinks for add ons.
 */
public interface ConnectApplinkManager {

    /**
     * Creates an {@link ApplicationLink} for a connect add-on.
     * @param addon The addon to create the applink for
     * @param baseUrl The baseurl of the connect addon
     * @param authType the authentication type, must be JWT
     * @param publicKey the publicKey used for asymmetric key encryption. Cannot be null if using JWT+RSA
     * @param addonUserKey the user-key of the add-on user; will be stored for later retrieval when we work out the {@link java.security.Principal} for incoming requests from this add-on
     */
    void createAppLink(ConnectAddonBean addon, String baseUrl, AuthenticationType authType, String publicKey, String addonUserKey);

    /**
     * Deletes an {@link ApplicationLink} for an Atlassian Connect add-on.
     * @param addon the addon to delete the applink for
     */
    void deleteAppLink(ConnectAddonBean addon) throws NotConnectAddonException;

    void deleteAppLink(String key) throws NotConnectAddonException;

    /**
     * Retrieves an {@link ApplicationLink} found for the Atlassian Connect add-on with given key
     * @param key plugin key for the connect add-on
     * @return the {@link ApplicationLink}, or null if either there are none or the plugin key is not associated with a Connect add-on
     * @throws com.atlassian.plugin.connect.plugin.auth.applinks.NotConnectAddonException if the key belongs to a plugin which is not a Connect add-on
     */
    ApplicationLink getAppLink(String key) throws NotConnectAddonException;

    /**
     * @param applink application link
     * @return the self link for this application link
     */
    URI getApplinkLinkSelfLink(ApplicationLink applink);

    /**
     * Reads the JWT shared secret or the OAuth consumer public key from an
     * applink
     *
     * @param applink the application link for the add-on
     * @return the JWT shared secret or the OAuth consumer public key
     */
    Optional<String> getSharedSecretOrPublicKey(ApplicationLink applink);

}
