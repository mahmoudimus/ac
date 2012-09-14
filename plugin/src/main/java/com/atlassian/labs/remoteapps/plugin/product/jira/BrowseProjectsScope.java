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
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/project", asList("get")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/issue", asList("get")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/search", asList("get"))
        )
        );
    }

    @Override
    public String getKey()
    {
        return "browse_projects";
    }
}
