package com.atlassian.plugin.remotable.test.pageobjects.jira;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.remotable.test.pageobjects.RemotePluginEmbeddedTestPage;

public class JiraViewIssuePageWithRemotePluginIssueTab extends RemotePluginEmbeddedTestPage implements Page
{
    private final String issueKey;

    public JiraViewIssuePageWithRemotePluginIssueTab(String issueKey)
    {
        super("issue-tab-page-jira-remotePluginIssueTabPage");
        this.issueKey = issueKey;
    }

    @Override
    public String getUrl()
    {
        return "/browse/" + issueKey + "?page=app1:issue-tab-page-jira-remotePluginIssueTabPage";
    }


}
