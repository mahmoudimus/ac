package com.atlassian.plugin.connect.plugin.oldscopes.confluence;

import com.atlassian.plugin.connect.api.confluence.ConfluencePermissions;

import static java.util.Arrays.asList;

public final class ManageAnonymousPermissionsScope extends ConfluenceScope
{
    public ManageAnonymousPermissionsScope()
    {
        super(ConfluencePermissions.MANAGE_ANONYMOUS_PERMISSIONS,
                asList(
                        "addAnonymousUsePermission",
                        "removeAnonymousUsePermission",
                        "addAnonymousViewUserProfilePermission",
                        "removeAnonymousViewUserProfilePermission"
                        )
                );
    }
}
