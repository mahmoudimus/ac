package com.atlassian.plugin.connect.plugin.oldscopes.jira;

import com.atlassian.plugin.connect.api.jira.JiraPermissions;
import com.atlassian.plugin.connect.spi.permission.scope.RestApiScopeHelper;

import static java.util.Arrays.asList;

public final class JiraCreateIssuesScope extends JiraScope
{
    public JiraCreateIssuesScope()
    {
        super(
                JiraPermissions.CREATE_ISSUES,
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
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/issue", asList("post"))
                )
        );
    }
}
