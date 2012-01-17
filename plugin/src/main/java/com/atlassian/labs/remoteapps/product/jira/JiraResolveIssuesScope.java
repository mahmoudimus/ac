package com.atlassian.labs.remoteapps.product.jira;

import com.atlassian.labs.remoteapps.modules.permissions.scope.RestApiScope;

import static java.util.Arrays.asList;

/**
 *
 */
public class JiraResolveIssuesScope extends JiraScope
{
    public JiraResolveIssuesScope()
    {
        super(
                asList(
                        "progressWorkflowAction",
                        "getComponents",
                        "getFieldsForAction",
                        "getIssueTypesForProject",
                        "getPriorities",
                        "getSecurityLevels",
                        "getStatuses",
                        "getSubTaskIssueTypesForProject",
                        "getVersions"
                ),
                asList(
                        new RestApiScope.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/issue", asList("get","post","put")),
                        new RestApiScope.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/component", asList("get","post","put","delete")),
                        new RestApiScope.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/priority", asList("get","post","put","delete")),
                        new RestApiScope.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/resolution", asList("get")),
                        new RestApiScope.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/version", asList("get","post","put","delete")),
                        new RestApiScope.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/status", asList("get","post","put","delete")),
                        new RestApiScope.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/worklog", asList("get","post","put","delete")),
                        new RestApiScope.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/custom", asList("get","post","put","delete")),
                        new RestApiScope.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/attachment", asList("get","post","put","delete")),
                        new RestApiScope.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/comment", asList("get","post","put","delete"))
                ));
    }
}
