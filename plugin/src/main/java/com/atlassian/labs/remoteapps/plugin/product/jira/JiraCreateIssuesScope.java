package com.atlassian.labs.remoteapps.plugin.product.jira;

import com.atlassian.labs.remoteapps.spi.permission.scope.RestApiScopeHelper;

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
}
