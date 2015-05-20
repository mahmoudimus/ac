package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.api.util.ConnectPluginInfo;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class JiraViewIssuePageWithRemotePluginIssueTab extends ConnectAddOnEmbeddedTestPage implements Page
{
    private final String issueKey;

    public JiraViewIssuePageWithRemotePluginIssueTab(String pageKey, String issueKey, String pluginKey)
    {
        super(pluginKey, pageKey, true);
        this.issueKey = issueKey;
    }

    @Override
    public String getUrl()
    {
        // e.g. /browse/ISSUE-1?page=com.atlassian.plugins.atlassian-connect-plugin:2tnm4dbyoxfktboogbmo__issue-tab-panel
        return "/browse/" + issueKey + "?page=" + ConnectPluginInfo.getPluginKey() + ":" + getTabLinkId();
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
