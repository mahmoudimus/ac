package com.atlassian.labs.remoteapps.product.jira;

import static java.util.Arrays.asList;

/**
 *
 */
public class JiraEditIssuesScope extends JiraScope
{
    public JiraEditIssuesScope()
    {
        super(asList(
                "updateIssue",
                "getComponents",
                "getFieldsForEdit",
                "getIssueTypesForProject",
                "getPriorities",
                "getSecurityLevels",
                "getStatuses",
                "getSubTaskIssueTypesForProject",
                "getVersions"
        ));
    }
}
