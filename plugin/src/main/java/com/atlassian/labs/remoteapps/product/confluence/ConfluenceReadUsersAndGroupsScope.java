package com.atlassian.labs.remoteapps.product.confluence;

import com.atlassian.labs.remoteapps.modules.permissions.scope.RestApiScopeHelper;

import static java.util.Arrays.asList;

/**
 *
 */
public class ConfluenceReadUsersAndGroupsScope extends ConfluenceScope
{
    public ConfluenceReadUsersAndGroupsScope()
    {
        super(asList(
                "getUser",
                "getUserGroups",
                "getGroups",
                "hasUser",
                "hasGroup",
                "getActiveUsers",
                "getUserInformation"
        ),
        asList(
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/user", asList("get"))
            )
        );
    }

    @Override
    public String getKey()
    {
        return "read_users_and_groups";
    }

    @Override
    public String getName()
    {
        return "Read Users and Groups";
    }

    @Override
    public String getDescription()
    {
        return "View users, users info, and groups";
    }
}
