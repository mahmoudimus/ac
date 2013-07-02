package com.atlassian.plugin.remotable.plugin.oldscopes.confluence;

import com.atlassian.plugin.remotable.api.confluence.ConfluencePermissions;

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
