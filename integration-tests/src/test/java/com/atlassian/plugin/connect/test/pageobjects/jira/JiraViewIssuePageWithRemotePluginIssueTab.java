package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class JiraViewIssuePageWithRemotePluginIssueTab extends ConnectAddOnEmbeddedTestPage implements Page
{
    private static final String DEFAULT_PAGE_KEY = "jira-remote-plugin-issue-tab-page";
    private final String issueKey;
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
        super(pluginKey, pageKey, true);
        this.issueKey = issueKey;
        this.pluginKey = pluginKey;
        this.pagePrefix = pagePrefix;
    }

    @Override
    public String getUrl()
    {
        return "/browse/" + issueKey + "?page=" + pagePrefix + getTabLinkId(); // com.atlassian.plugins.atlassian-connect-plugin:2tnm4dbyoxfktboogbmo__issue-tab-panel
    }

    public String getTabName()
    {
        WebElement tab = driver.findElement(By.id(getTabLinkId()));
        return tab.getText();
    }

    private String getTabLinkId()
    {
        return ModuleKeyUtils.addonAndModuleKey(addOnKey, pageElementKey);
    }
}
