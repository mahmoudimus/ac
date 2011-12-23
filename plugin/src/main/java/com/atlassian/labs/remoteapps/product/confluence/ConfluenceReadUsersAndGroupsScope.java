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
}
