package com.atlassian.plugin.connect.plugin.oldscopes.jira;

import com.atlassian.plugin.connect.api.jira.JiraPermissions;
import com.atlassian.plugin.connect.spi.permission.scope.RestApiScopeHelper;

import static java.util.Arrays.asList;

public final class JiraReadUsersAndGroupsScope extends JiraScope
{
    public JiraReadUsersAndGroupsScope()
    {
        super(
                JiraPermissions.READ_USERS_AND_GROUPS,
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
}
