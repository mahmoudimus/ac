package com.atlassian.plugin.remotable.plugin.oldscopes.confluence;

import com.atlassian.plugin.remotable.api.confluence.ConfluencePermissions;
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
