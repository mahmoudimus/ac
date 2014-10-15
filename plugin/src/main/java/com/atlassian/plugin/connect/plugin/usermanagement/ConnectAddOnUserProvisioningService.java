package com.atlassian.plugin.connect.plugin.usermanagement;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;

import java.util.Set;

public interface ConnectAddOnUserProvisioningService
{
    public static String USER_PROVISIONING_ERROR = "connect.install.error.user.provisioning";
    public static String ADDON_ADMINS_MISSING_PERMISSION = "connect.install.error.addon.admin.permission";

    void provisionAddonUserForScopes(String username, Set<ScopeName> previousScopes, Set<ScopeName> newScopes) throws ConnectAddOnUserInitException;

    /**
     * The keys of product groups of which add-on users should by default be members. Don't create these groups if they
     * don't exist, because they are managed by the products.
     *
     * @return {@link java.util.Set} of group keys (for example ["confluence-users"]).
     */
    public Set<String> getDefaultProductGroups();
}
