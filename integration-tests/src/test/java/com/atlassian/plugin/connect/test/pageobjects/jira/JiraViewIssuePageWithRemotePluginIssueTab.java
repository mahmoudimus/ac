package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class JiraViewIssuePageWithRemotePluginIssueTab extends RemotePluginEmbeddedTestPage implements Page
{
    public static final String DEFAULT_PAGE_KEY = "issue-tab-page-jira-remotePluginIssueTabPage";
    private final String issueKey;
    private final String pageKey;
    private final String pluginKey;
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
        super(pageKey);
        this.pageKey = pageKey;
        this.issueKey = issueKey;
        this.pluginKey = pluginKey;
        this.pagePrefix = pagePrefix;
    }

    @Override
    public String getUrl()
    {
        return "/browse/" + issueKey + "?page=" + pagePrefix + key;
    }

    public String getTabName()
    {
        WebElement tab = driver.findElement(By.id(pageKey));
        return tab.getText();
    }

}
