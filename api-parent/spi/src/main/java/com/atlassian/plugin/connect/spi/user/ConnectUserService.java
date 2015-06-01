package com.atlassian.plugin.connect.spi.user;

import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.sal.api.user.UserKey;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Allows creating and managing users for connect add-ons and give connect access to user properties (most notably
 * whether a user is active).
 *
 * @since 1.1.33
 */
public interface ConnectUserService
{
    /**
     * Retrieves the {@link UserKey user key} for the add-on user. If the user does not exist, the add-on user is
     * created.
     *
     * @param addonKey the add-on key
     * @param addonDisplayName the add-on display name
     * @return the add-on user's {@link UserKey key}
     * @throws ConnectAddOnUserInitException when the connect add-on user could not be created
     */
    @Nonnull
    UserKey getUserKeyForAddon(@Nonnull String addonKey, @Nonnull String addonDisplayName) throws ConnectAddOnUserInitException;

    /**
     * @param userKey the user's {@link UserKey key}
     * @return {@code true} if the user is active, {@code false} if the user is not active or does not exist
     */
    boolean isUserActive(@Nonnull UserKey userKey);

    /**
     * Activates or de-activates the add-on user
     * @param addonKey the add-on key
     * @param active whether the user should be actived or de-activated
     * @throws ConnectAddOnUserInitException when the user could not be updated
     */
    void setUserActiveForAddon(@Nonnull String addonKey, boolean active) throws ConnectAddOnUserInitException;

    /**
     * Configures the scopes for the add-on user. If the add-on user does not exist, one will be created.
     *
     * @param addonKey the add-on key
     * @param addonDisplayName the add-on display name
     * @param previousScopes the previous set of scopes
     * @param newScopes the target set of scopes
     * @return the add-on user's {@link UserKey key}
     * @throws ConnectAddOnUserInitException
     */
    @Nonnull
    UserKey setUserScopesForAddon(@Nonnull String addonKey, @Nonnull String addonDisplayName,
                               @Nonnull Set<ScopeName> previousScopes, @Nonnull Set<ScopeName> newScopes) throws ConnectAddOnUserInitException;
}
