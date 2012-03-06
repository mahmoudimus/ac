package com.atlassian.labs.remoteapps.product.jira;

import com.atlassian.labs.remoteapps.modules.permissions.scope.RestApiScopeHelper;

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

    @Override
    public String getName()
    {
        return "Edit Issues";
    }

    @Override
    public String getDescription()
    {
        return "Permission to edit issues (excluding the  'Due Date' field - see the Schedule " +
                "Issues permission). Includes the ability to convert issues to sub-tasks and vice " +
                "versa (if sub-tasks are enabled). Note that the Delete Issue permission is " +
                "required in order to delete issues.";
    }
}
