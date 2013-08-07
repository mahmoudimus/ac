package com.atlassian.plugin.connect.plugin.oldscopes.confluence;

import com.atlassian.plugin.connect.api.confluence.ConfluencePermissions;
import com.atlassian.plugin.connect.spi.permission.scope.RestApiScopeHelper;

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
                        "getUser",
                        "getUserByName",
                        "getUserByKey",
                        "hasUser",
                        "hasGroup"
                ),
                asList(
                        new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/user", asList("get"))
                )
        );
    }
}
