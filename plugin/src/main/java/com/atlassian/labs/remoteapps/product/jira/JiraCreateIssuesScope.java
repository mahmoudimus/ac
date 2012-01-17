package com.atlassian.labs.remoteapps.product.jira;

import com.atlassian.labs.remoteapps.modules.permissions.scope.RestApiScope;

import static java.util.Arrays.asList;

/**
 *
 */
public class JiraCreateIssuesScope extends JiraScope
{
    public JiraCreateIssuesScope()
    {
        super(
                asList(
                        "createIssue",
                        "createIssueWithParent",
                        "createIssueWithParentWithSecurityLevel",
                        "createIssueWithSecurityLevel",
                        "getComponents",
                        "getFieldsForCreate",
                        "getIssueTypesForProject",
                        "getPriorities",
                        "getSecurityLevels",
                        "getStatuses",
                        "getSubTaskIssueTypesForProject",
                        "getVersions"
                ),
                asList(
                        new RestApiScope.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/issue", asList("post"))
                )
        );
    }
}
