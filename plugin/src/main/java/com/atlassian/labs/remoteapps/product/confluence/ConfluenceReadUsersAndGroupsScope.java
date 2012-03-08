package com.atlassian.labs.remoteapps.product.confluence;

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
        ));
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
