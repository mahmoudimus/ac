package com.atlassian.labs.remoteapps.test.jira;

import com.atlassian.labs.remoteapps.test.RemoteAppEmbeddedTestPage;
import com.atlassian.pageobjects.Page;

public class JiraViewIssuePageWithRemoteAppIssueTab extends RemoteAppEmbeddedTestPage implements Page
{
    private final String issueKey;

    public JiraViewIssuePageWithRemoteAppIssueTab(String issueKey)
    {
        super("issue-tab-page-jira-remoteAppIssueTabPage");
        this.issueKey = issueKey;
    }

    @Override
    public String getUrl()
    {
        return "/browse/" + issueKey + "?page=app1:issue-tab-page-jira-remoteAppIssueTabPage";
    }


}
