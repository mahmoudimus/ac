package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.model.user.User;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConnectAddonUserUtil {
    public static String usernameForAddon(String addonKey) {
        checkNotNull(addonKey);
        return Constants.ADDON_USERNAME_PREFIX + addonKey;
    }

    /**
     * Will return the a Map containing a value that will mark the Connect Addon user as a 'Connect User'. This will be a 'synch-able'
     * Crowd attribute (shared attribute across all Crowd connected applications) when this is finally enabled in Crowd.
     * @param applicationName the name of the application the attribute was created from. This is mostly for an audit trail.
     * @return An ImmutableMap allowing the marking of a user as a Connect Addon user
     */
    public static ImmutableMap<String, Set<String>> buildConnectAddonUserAttribute(String applicationName) {
        return ImmutableMap.of(buildAttributeConnectAddonAttributeName(applicationName), Collections.singleton("true"));
    }

    /**
     * Builds the Connect Addon attribute name that is used to store against Connect Addon Users, which 'marks' them as a Connect Addon.
     * @param applicationName the name of the application the attribute was created from. This is mostly for an audit trail.
     * @return String representation of the Connect Addon User attribute name. <em>synch.APPLICATION_NAME.atlassian-connect-user</em>
     */
    public static String buildAttributeConnectAddonAttributeName(String applicationName) {
        return "synch." + applicationName + ".atlassian-connect-user";
    }

    /**
     * Will validate that a user's username adheres to the correct Addon format
     * @param user the user to validate
     * @return true if the user has a valid username
     */
    public static boolean validAddonUsername(User user) {
        String name = user.getName();
        return name != null && name.startsWith(Constants.ADDON_USERNAME_PREFIX);
    }

    /**
     * Will validate that the user has an email address that equals the desired Addon email ({@link Constants#ADDON_USER_EMAIL_ADDRESS}).
     * @param user the user to validate
     * @return true if the email address is valid
     */
    public static boolean validAddonEmailAddress(User user) {
        return Constants.ADDON_USER_EMAIL_ADDRESS.equals(user.getEmailAddress());
    }

    public static class Constants {
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
