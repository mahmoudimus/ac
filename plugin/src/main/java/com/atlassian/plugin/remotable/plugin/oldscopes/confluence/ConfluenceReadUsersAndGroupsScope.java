package com.atlassian.plugin.remotable.plugin.oldscopes.confluence;

import com.atlassian.plugin.remotable.api.confluence.ConfluencePermissions;
import com.atlassian.plugin.remotable.spi.permission.scope.RestApiScopeHelper;

import static java.util.Arrays.asList;

public final class ConfluenceReadUsersAndGroupsScope extends ConfluenceScope
{
    public ConfluenceReadUsersAndGroupsScope()
    {
        super(ConfluencePermissions.READ_USERS_AND_GROUPS,
                asList(
                        "getGroups",
                        "isActiveUser",
                        "getActiveUsers",
                        "getUserInformation",
                        "getUserPreferenceBoolean",
                        "getUserPreferenceLong",
                        "getUserPreferenceString",
                        "hasUser",
                        "hasGroup"
                ),
                asList(
                        new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/user", asList("get"))
                )
        );
    }
}
