package com.atlassian.labs.remoteapps.plugin.product.jira;

import com.atlassian.labs.remoteapps.spi.permission.scope.RestApiScopeHelper;

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
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/issue", asList("put","delete"))
                )
        );
    }

    @Override
    public String getKey()
    {
        return "edit_issues";
    }
}
