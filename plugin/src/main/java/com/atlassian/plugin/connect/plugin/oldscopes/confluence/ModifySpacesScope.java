package com.atlassian.plugin.connect.plugin.oldscopes.confluence;

import com.atlassian.plugin.connect.api.confluence.ConfluencePermissions;

import static java.util.Arrays.asList;

public final class ModifySpacesScope extends ConfluenceScope
{
    public ModifySpacesScope()
    {
        super(ConfluencePermissions.MODIFY_SPACES,
                asList(
                        "addSpaceWithDefaultPermissions",
                        "addSpace",
                        "storeSpace",
                        "addPersonalSpaceWithDefaultPermissions",
                        "addPersonalSpace",
                        "addPermissionToSpace",
                        "addPermissionsToSpace",
                        "removePermissionFromSpace",
                        "removeSpace",
                        "setSpaceStatus"
                )
                );
    }
}
