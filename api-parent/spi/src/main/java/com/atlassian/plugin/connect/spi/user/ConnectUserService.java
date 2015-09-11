package com.atlassian.plugin.connect.spi.user;

import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;

/**
 * Provided by the host application to allow connect to manage add-on users and check whether impersonated users
 * are active.
 */
public interface ConnectUserService
{
    /**
     * Disables the add-on user for the add-on with key {@code addOnKey}
     *
     * @param addOnKey the key of the add-on
     * @throws ConnectAddOnUserDisableException
     */
    void disableAddOnUser(@Nonnull String addOnKey) throws ConnectAddOnUserDisableException;

    /**
     * Retrieves the username for the add-on with key {@code addOnKey}. If the user does not exist, the user is created.
     * If the user exists but is disabled, the user is activated.
     *
     * @param addOnKey the key of the add-on
     * @param addOnDisplayName the display name of the add-on
     * @return the user the username for the add-on user
     * @throws ConnectAddOnUserInitException
     */
    @Nonnull
    String getOrCreateAddOnUserName(@Nonnull String addOnKey, @Nonnull String addOnDisplayName) throws ConnectAddOnUserInitException;

    /**
     * Checks whether the user with the provided {@code username} is active.
     *
     * @param username the username to check
     * @return {@code true} if the user exists and is active, otherwise {@code false}
     */
    boolean isActive(@Nonnull String username);

    /**
     * Provisions an add-on user for the add-on with key {@code addOnKey} and configures the user for the provided set
     * of {@link ScopeName scopes}. If the user already exists and is disabled, the user is re-enabled.
     *
     * @param addOnKey the key of the add-on
     * @param addOnDisplayName the display name of the add-on
     * @param previousScopes the set of previous scopes in the case of a re-install of the add-on
     * @param newScopes the set of requested scopes
     * @return the username for the add-on user
     * @throws ConnectAddOnUserInitException
     */
    @Nonnull
    String provisionAddOnUserForScopes(@Nonnull String addOnKey, @Nonnull String addOnDisplayName,
            @Nonnull Set<ScopeName> previousScopes, @Nonnull Set<ScopeName> newScopes) throws ConnectAddOnUserInitException;
}
