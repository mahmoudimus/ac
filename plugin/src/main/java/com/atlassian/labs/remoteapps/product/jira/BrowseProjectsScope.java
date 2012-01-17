package com.atlassian.labs.remoteapps.product.jira;

import com.atlassian.labs.remoteapps.modules.permissions.scope.RestApiScope;

import static java.util.Arrays.asList;

/**
 *
 */
public class BrowseProjectsScope extends JiraScope
{
    public BrowseProjectsScope()
    {
        super(
                asList(
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
                ),
                asList(
                        new RestApiScope.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/project", asList("get"))
                )
        );
    }
}
