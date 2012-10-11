package com.atlassian.plugin.remotable.plugin.product.jira;

import com.atlassian.plugin.remotable.spi.permission.scope.RestApiScopeHelper;

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
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/user", asList("get")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/group", asList("get")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/mypermissions", asList("get"))
                ));
    }

    @Override
    public String getKey()
    {
        return "read_users_and_groups";
    }
}
