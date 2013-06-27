package com.atlassian.plugin.remotable.plugin.oldscopes.confluence;

import com.atlassian.plugin.remotable.api.confluence.ConfluencePermissions;

import static java.util.Arrays.asList;

/**
 * API Scope for Confluence that grants Remotable Plugins the ability to change the details of user accounts in Confluence.
 */
public final class ModifyUsersScope extends ConfluenceScope
{
    public ModifyUsersScope()
    {
        super(ConfluencePermissions.MODIFY_USERS,
                asList(
                        "editUser",
                        "setUserInformation",
                        "setUserPreferenceBoolean",
                        "setUserPreferenceLong",
                        "setUserPreferenceString",
                        "addProfilePicture"
                )
                );
    }
}
