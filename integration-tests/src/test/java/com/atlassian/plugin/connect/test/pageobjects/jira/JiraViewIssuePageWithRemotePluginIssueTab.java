package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;

public class JiraViewIssuePageWithRemotePluginIssueTab extends RemotePluginEmbeddedTestPage implements Page
{
    private static final String DEFAULT_PAGE_KEY = "issue-tab-page-jira-remotePluginIssueTabPage";
    private final String issueKey;
    private final String pluginKey;

    public JiraViewIssuePageWithRemotePluginIssueTab(String issueKey, String pluginKey)
    {
        this(DEFAULT_PAGE_KEY, issueKey, pluginKey);
    }

    public JiraViewIssuePageWithRemotePluginIssueTab(String pageKey, String issueKey, String pluginKey)
    {
        super(pageKey);
        this.issueKey = issueKey;
        this.pluginKey = pluginKey;
    }

    @Override
    public String getUrl()
    {
        return "/browse/" + issueKey + "?page=" + pluginKey + ModuleKeyUtils.ADDON_MODULE_SEPARATOR + key;
    }


}
