package com.atlassian.labs.remoteapps.product.jira;

import com.atlassian.labs.remoteapps.modules.permissions.scope.RestApiScopeHelper;

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
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/issue", asList("get","post","put")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/component", asList("get","post","put","delete")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/priority", asList("get","post","put","delete")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/resolution", asList("get")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/version", asList("get","post","put","delete")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/status", asList("get","post","put","delete")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/worklog", asList("get","post","put","delete")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/custom", asList("get","post","put","delete")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/attachment", asList("get","post","put","delete")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/comment", asList("get","post","put","delete"))
                ));
    }

    @Override
    public String getKey()
    {
        return "resolve_issues";
    }

    @Override
    public String getName()
    {
        return "Resolve Issues";
    }

    @Override
    public String getDescription()
    {
        return "Permission to resolve and reopen issues. This also includes the ability to set " +
                "the 'Fix For version' field for issues. Also see the Close Issues permission.";
    }
}
