package com.atlassian.plugin.connect.plugin.usermanagement;

import java.util.Set;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;

public interface ConnectAddOnUserProvisioningService
{
    void provisionAddonUserForScopes(String userKey, Set<ScopeName> scopes);

    /**
     * The keys of product groups of which add-on users should by default be members. Don't create these groups if they
     * don't exist, because they are managed by the products.
     *
     * @return {@link java.util.Set} of group keys (for example ["confluence-users"]).
     */
    public Set<String> getDefaultProductGroups();

    /**
     * Make this group an administrators group if it is not already.
     *
     * @param groupKey the key identifying the group
     */
    void ensureGroupHasProductAdminPermission(String groupKey);

    /**
     * Determine whether or not this group is an administrators group.
     *
     * @param groupKey the key identifying the group
     * @return {@code true} if the group itself has administrator privileges, otherwise {@code false}
     */
    boolean groupHasProductAdminPermission(String groupKey);
}
