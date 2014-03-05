package com.atlassian.plugin.connect.plugin.usermanagement;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;

import java.util.Set;

public interface ConnectAddOnUserProvisioningService
{
    void provisionAddonUserForScopes(String userKey, Set<ScopeName> scopes) throws ApplicationPermissionException, ApplicationNotFoundException, OperationFailedException, ConnectAddOnUserInitException;

    /**
     * The keys of product groups of which add-on users should by default be members. Don't create these groups if they
     * don't exist, because they are managed by the products.
     *
     * @return {@link java.util.Set} of group keys (for example ["confluence-users"]).
     */
    public Set<String> getDefaultProductGroups();
}
