package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;

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

    public void establishScopePermissions(User addOnUser, ScopeName scope);

    public void removeScopePermissions(User addOnUser, ScopeName scope);
}
