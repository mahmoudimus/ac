package com.atlassian.plugin.connect.plugin.usermanagement;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConnectAddOnUserUtil
{
    public static String  usernameForAddon(String addonKey)
    {
        checkNotNull(addonKey);
        return Constants.ADDON_USERNAME_PREFIX + addonKey;
    }

    /**
     * Will return the a Map containing a value that will mark the Connect AddOn user as a 'Connect User'. This will be a 'synch-able'
     * Crowd attribute (shared attribute across all Crowd connected applications) when this is finally enabled in Crowd.
     * @param applicationName the name of the application the attribute was created from. This is mostly for an audit trail.
     * @return An ImmutableMap allowing the marking of a user as a Connect AddOn user
     * @throws com.atlassian.crowd.exception.ApplicationNotFoundException
     */
    public static ImmutableMap<String, Set<String>> buildConnectAddOnUserAttribute(String applicationName) throws ApplicationNotFoundException
    {
        return ImmutableMap.of(buildAttributeConnectAddOnAttributeName(applicationName), Collections.singleton("true"));
    }

    /**
     * Builds the Connect AddOn attribute name that is used to store against Connect AddOn Users, which 'marks' them as a Connect AddOn.
     * @param applicationName the name of the application the attribute was created from. This is mostly for an audit trail.
     * @return String representation of the Connect AddOn User attribute name. <em>synch.APPLICATION_NAME.atlassian-connect-user</em>
     */
    public static String buildAttributeConnectAddOnAttributeName(String applicationName)
    {
        return "synch." + applicationName + ".atlassian-connect-user";
    }

    public static class Constants
    {
        /**
         * All Addon usernames are prefixed with this string
         */
        public static final String ADDON_USERNAME_PREFIX = "addon_";

        /**
         * Use a "no reply" email address for add-on users so that
         *     - reset password attempts are not received by anyone, and
         *     - there are no error messages in logs about failing to email.
         * Note that an admin can still change the email address but that non-admins can't simply click a
         * "I lost my password" link and take control of the account.
         *
         * We also rely on the user-provisioning-plugin to count add-on users as consuming licenses if an admin
         * does take control of an account and use it to log in.
         *
         * The rationale is that either they can't log in as these users, in which case they consume no licenses,
         * or logging in is possible and such users do consume licenses.
         */
        public static final String ADDON_USER_EMAIL_ADDRESS = "noreply@mailer.atlassian.com";

        /**
         * The group which is created to house all Atlassian Connect add-on users. In order to not occupy a license this
         * has to match constant in user-provisioning-plugin/src/main/java/com/atlassian/crowd/plugin/usermanagement/userprovisioning/Constants.java
         */
        public static final String ADDON_USER_GROUP_KEY = "atlassian-addons";
    }
}
