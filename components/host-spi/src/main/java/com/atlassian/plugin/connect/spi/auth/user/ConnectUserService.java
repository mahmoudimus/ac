package com.atlassian.plugin.connect.spi.auth.user;

import com.atlassian.plugin.connect.api.lifecycle.ConnectAddonDisableException;
import com.atlassian.plugin.connect.api.lifecycle.ConnectAddonInitException;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Provided by the host application to allow connect to manage add-on users and check whether impersonated users
 * are active.
 */
public interface ConnectUserService {
    /**
     * Disables the add-on user for the add-on with key {@code addonKey}
     *
     * @param addonKey the key of the add-on
     * @throws ConnectAddonDisableException
     */
    void disableAddonUser(@Nonnull String addonKey) throws ConnectAddonDisableException;

    /**
     * Retrieves the username for the add-on with key {@code addonKey}. If the user does not exist, the user is created.
     * If the user exists but is disabled, the user is activated.
     *
     * @param addonKey the key of the add-on
     * @param addonDisplayName the display name of the add-on
     * @return the user the username for the add-on user
     * @throws ConnectAddonInitException
     */
    @Nonnull
    String getOrCreateAddonUserName(@Nonnull String addonKey, @Nonnull String addonDisplayName) throws ConnectAddonInitException;

    /**
     * Checks whether the user with the provided {@code username} is active.
     *
     * @param username the username to check
     * @return {@code true} if the user exists and is active, otherwise {@code false}
     */
    boolean isActive(@Nonnull String username);

    /**
     * Provisions an add-on user for the add-on with key {@code addonKey} and configures the user for the provided set
     * of {@link ScopeName scopes}. If the user already exists and is disabled, the user is re-enabled.
     *
     * @param addon the add-on
     * @param previousScopes the set of previous scopes in the case of a re-install of the add-on
     * @param newScopes the set of requested scopes
     * @return the username for the add-on user, or null if none is required
     * @throws ConnectAddonInitException
     */
    @Nonnull
    String provisionAddonUserWithScopes(@Nonnull ConnectAddonBean addon,
                                        @Nonnull Set<ScopeName> previousScopes, @Nonnull Set<ScopeName> newScopes) throws ConnectAddonInitException;
}
