package com.atlassian.labs.remoteapps.product.jira;

import static java.util.Arrays.asList;

/**
 *
 */
public class BrowseProjectsScope extends JiraScope
{
    public BrowseProjectsScope()
    {
        super(asList(
                "getAttachmentsFromIssue",
                "getComment",
                "getComments",
                "getIssue",
                "getIssueById",
                "getIssuesFromJqlSearch",
                "getIssuesFromTextSearchWithLimit",
                "getIssuesFromTextSearchWithProject",
                "getProjectAvatar",
                "getProjectAvatars",
                "getProjectById",
                "getProjectByKey",
                "getProjectsNoSchemes",
                "getResolutionDateById",
                "getResolutionDateByKey",
                "getResolutions",
                "getSecurityLevel"
        ));
    }
}
