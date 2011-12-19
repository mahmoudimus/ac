package com.atlassian.labs.remoteapps.product.jira;

import static java.util.Arrays.asList;

/**
 *
 */
public class JiraResolveIssuesScope extends JiraScope
{
    public JiraResolveIssuesScope()
    {
        super(asList(
                "progressWorkflowAction",
                "getComponents",
                "getFieldsForAction",
                "getIssueTypesForProject",
                "getPriorities",
                "getSecurityLevels",
                "getStatuses",
                "getSubTaskIssueTypesForProject",
                "getVersions"
        ));
    }
}
