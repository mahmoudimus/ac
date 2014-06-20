package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;

public class JiraViewIssuePageWithRemotePluginIssueTab extends ConnectAddOnEmbeddedTestPage implements Page
{
    private static final String DEFAULT_PAGE_KEY = "jira-remote-plugin-issue-tab-page";
    private final String issueKey;
    private final String pagePrefix;

    public JiraViewIssuePageWithRemotePluginIssueTab(String issueKey, String pluginKey)
    {
        this(DEFAULT_PAGE_KEY, issueKey, pluginKey,"");
    }

    public JiraViewIssuePageWithRemotePluginIssueTab(String issueKey, String pluginKey, String pagePrefix)
    {
        this(DEFAULT_PAGE_KEY, issueKey, pluginKey, pagePrefix);
    }

    public JiraViewIssuePageWithRemotePluginIssueTab(String pageKey, String issueKey, String pluginKey, String pagePrefix)
    {
        super(pageKey, pluginKey, false);
        this.issueKey = issueKey;
        this.pagePrefix = pagePrefix;
    }

    @Override
    public String getUrl()
    {
        return "/browse/" + issueKey + "?page=" + pagePrefix + pageElementKey;
    }


}
