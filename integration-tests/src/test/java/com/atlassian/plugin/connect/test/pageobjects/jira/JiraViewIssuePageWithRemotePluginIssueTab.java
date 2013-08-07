package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;

public class JiraViewIssuePageWithRemotePluginIssueTab extends RemotePluginEmbeddedTestPage implements Page
{
    private final String issueKey;
    private final String pluginKey;

    public JiraViewIssuePageWithRemotePluginIssueTab(String issueKey, String pluginKey)
    {
        super("issue-tab-page-jira-remotePluginIssueTabPage");
        this.issueKey = issueKey;
        this.pluginKey = pluginKey;
    }

    @Override
    public String getUrl()
    {
        return "/browse/" + issueKey + "?page=" + pluginKey + ":issue-tab-page-jira-remotePluginIssueTabPage";
    }


}
