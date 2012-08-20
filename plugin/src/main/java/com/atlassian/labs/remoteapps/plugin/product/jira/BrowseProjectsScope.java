package com.atlassian.labs.remoteapps.plugin.product.jira;

import com.atlassian.labs.remoteapps.spi.permission.scope.RestApiScopeHelper;

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
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/project", asList("get"))
                )
        );
    }

    @Override
    public String getKey()
    {
        return "browse_projects";
    }

    @Override
    public String getName()
    {
        return "Browse Projects";
    }

    @Override
    public String getDescription()
    {
        return "Permission to browse projects, search issues, and view individual issues (except " +
                "issues that have been restricted via Issue Security)";
    }
}
