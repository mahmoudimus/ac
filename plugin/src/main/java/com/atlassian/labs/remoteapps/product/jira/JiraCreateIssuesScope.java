package com.atlassian.labs.remoteapps.product.jira;

import com.atlassian.labs.remoteapps.modules.permissions.scope.RestApiScopeHelper;

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
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/issue", asList("post"))
                )
        );
    }

    @Override
    public String getKey()
    {
        return "create_issues";
    }

    @Override
    public String getName()
    {
        return "Create Issues";
    }

    @Override
    public String getDescription()
    {
        return "Permission to create issues in the project. (Note that the Create Attachments " +
                "permission is required in order to create attachments.) Includes the ability to " +
                "create sub-tasks (if sub-tasks are enabled).";
    }
}
