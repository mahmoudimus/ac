package com.atlassian.labs.remoteapps.product.jira;

import com.atlassian.labs.remoteapps.modules.permissions.scope.RestApiScope;

import static java.util.Arrays.asList;

/**
 *
 */
public class JiraReadUsersAndGroupsScope extends JiraScope
{
    public JiraReadUsersAndGroupsScope()
    {
        super(
                asList(
                        "getUser",
                        "getGroup"
                ),
                asList(
                        new RestApiScope.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/user", asList("get")),
                        new RestApiScope.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/group", asList("get"))
                ));
    }
}
