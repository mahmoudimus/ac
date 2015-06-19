package com.atlassian.plugin.connect.api.usermanagment;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.sal.api.user.UserKey;

import java.util.Set;

import javax.annotation.Nonnull;

public interface ConnectAddOnUserProvisioningService
{
    public static String USER_PROVISIONING_ERROR = "connect.install.error.user.provisioning";
    public static String ADDON_ADMINS_MISSING_PERMISSION = "connect.install.error.addon.admin.permission";

    void provisionAddonUserForScopes(@Nonnull UserKey userKey, @Nonnull Set<ScopeName> previousScopes,
            @Nonnull Set<ScopeName> newScopes) throws ConnectAddOnUserInitException;

    /**
     * The keys of product groups of which add-on users should by default be members, and all of which are expected to exist
     * Don't create these groups if they don't exist, because they are managed by the products.
     *
     * @return {@link java.util.Set} of group keys (for example ["_licensed-confluence"]).
     */
    @Nonnull
    Set<String> getDefaultProductGroupsAlwaysExpected();

    /**
     * The keys of product groups, one or more of which add-on users should by default be members.
     * Some of these groups might not exist in an instance but we expect at least one to.
     * Don't create these groups if they don't exist, because they are managed by the products.
     *
     * @return {@link java.util.Set} of group keys (for example ["confluence-users"]).
     */
    @Nonnull
    Set<String> getDefaultProductGroupsOneOrMoreExpected();
}
