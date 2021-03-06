package com.atlassian.plugin.connect.crowd.spi;

import com.atlassian.plugin.connect.api.lifecycle.ConnectAddonInitException;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;

import java.util.Set;

public interface CrowdAddonUserProvisioningService {
    void provisionAddonUserForScopes(String username, Set<ScopeName> previousScopes, Set<ScopeName> newScopes) throws ConnectAddonInitException;

    /**
     * The keys of product groups of which add-on users should by default be members, and all of which are expected to exist
     * Don't create these groups if they don't exist, because they are managed by the products.
     *
     * @return {@link java.util.Set} of group keys (for example ["_licensed-confluence"]).
     */
    Set<String> getDefaultProductGroupsAlwaysExpected();

    /**
     * The keys of product groups, one or more of which add-on users should by default be members.
     * Some of these groups might not exist in an instance but we expect at least one to.
     * Don't create these groups if they don't exist, because they are managed by the products.
     *
     * @return {@link java.util.Set} of group keys (for example ["confluence-users"]).
     */
    Set<String> getDefaultProductGroupsOneOrMoreExpected();
}
