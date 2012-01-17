package com.atlassian.labs.remoteapps.product.jira;

import com.atlassian.labs.remoteapps.modules.permissions.scope.RestApiScope;

import static java.util.Arrays.asList;

/**
 *
 */
public class JiraEditIssuesScope extends JiraScope
{
    public JiraEditIssuesScope()
    {
        super(
                asList(
                        "updateIssue",
                        "getComponents",
                        "getFieldsForEdit",
                        "getIssueTypesForProject",
                        "getPriorities",
                        "getSecurityLevels",
                        "getStatuses",
                        "getSubTaskIssueTypesForProject",
                        "getVersions"
                ),
                asList(
                        new RestApiScope.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/issue", asList("put","delete"))
                )
        );
    }
}
