package com.atlassian.plugin.connect.plugin.usermanagement;

import java.util.Set;

/**
 * Manage user group membership for Connect add-ons.
 * Product-specific implementations are expected.
 */
public interface ConnectAddOnUserGroupsService
{
    /**
     * The keys of product groups of which add-on users should by default be members.
     * Don't create these groups if they don't exist, because they are managed by the products.
     * @return {@link Set} of group keys (for example ["confluence-users"]).
     */
    public Set<String> getDefaultProductGroups();

    /**
     * Make this group an administrators group if it is not already.
     * @param groupKey the key identifying the group
     */
    void ensureIsAdmin(String groupKey);

    /**
     * Determine whether or not this group is an administrators group.
     * @param groupKey the key identifying the group
     * @return {@code true} if the group itself has administrator privileges, otherwise {@code false}
     */
    boolean isAdmin(String groupKey);
}
